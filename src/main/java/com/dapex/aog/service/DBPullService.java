package com.dapex.aog.service;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.*;
import com.dapex.aog.exception.UpdateFailureException;
import com.dapex.aog.jpa.domain.*;
import com.dapex.aog.jpa.repo.*;
import com.dapex.aog.utils.ProcessingUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dapex.aog.utils.ProcessingUtility.htmlToRaw;

/**
 * <p>Service class to create a cached AOG from the database,
 * search and return an AOG from memory and
 * it also returns a list of AOGs with their ids</p>
 * Created by mmacpher on 12/4/18.
 */
@Service
@CacheConfig(cacheNames = {"AOG"})
public class DBPullService {

    private static final Logger LOGGER = LogManager.getLogger();

    private AttachmentsRepository attachmentsRepository;

    private OptionCategoryNARepository optionCategoryNARepository;

    private OptionHeaderRepository optionHeaderRepository;

    private OptionHierarchyRepository optionHierarchyRepository;

    private OptionReviewRepository optionReviewRepository;

    @Autowired
    DBPullService(AttachmentsRepository attachmentsRepository,
                  OptionCategoryNARepository optionCategoryNARepository,
                  OptionHeaderRepository optionHeaderRepository,
                  OptionHierarchyRepository optionHierarchyRepository,
                  OptionReviewRepository optionReviewRepository) {
        this.attachmentsRepository = attachmentsRepository;
        this.optionCategoryNARepository = optionCategoryNARepository;
        this.optionHeaderRepository = optionHeaderRepository;
        this.optionHierarchyRepository = optionHierarchyRepository;
        this.optionReviewRepository = optionReviewRepository;
    }

    /**
     * <p>Gets the list of all the AOGs</p>
     *
     * @return Returns a list of all the AOGs with their ids and names
     */
    public List<AOG> getAOGList() {
        List<OptionHierarchy> optionHierarchies = this.optionHierarchyRepository.findByParentIdIsNull();
        List<AOG> aogs = new ArrayList<>();

        for (OptionHierarchy aog : optionHierarchies) {
            AOG newAOG = new AOG();
            newAOG.setAog(aog.getLabelTxt());
            newAOG.setId(aog.getId());
            aogs.add(newAOG);
        }

        return aogs;
    }

    /**
     * Retrieves a list of chapters from the Database
     *
     * @param id the id of the AOG that contains the chapter
     * @return A list of all chapters contained in the AOG
     */
    public List<Chapters> getChapterList(Long id) {
        List<OptionHierarchy> chaps = this.optionHierarchyRepository
                .findByParentIdAndLabel(id, Constants.CHAPTER_NAME);

        List<Chapters> chaptersList = new ArrayList<>();

        for (OptionHierarchy chap : chaps) {
            Chapters chapters = new Chapters(chap.getLabelTxt(), chap.getId());
            chaptersList.add(chapters);
        }

        return chaptersList;
    }

    /**
     * <p>Returns the AOG from in memory cache</p>
     *
     * @param id The id of the AOG to be looked up in the cache
     * @return Returns the AOG specified by the id
     */
    @Cacheable
    public AOGReturn getFullAOG(Long id) {
        AOGReturn aogReturn = new AOGReturn();
        return aogReturn;
    }

    /**
     * Get the hierarchy id of the chapter that contains the given id
     *
     * @param id unique id of a container
     * @return hiearchy id of a chapter
     */
    private Long getContainerChapterId(Long id) {
        if (containerExistsDB(id)) {
            OptionHierarchy container = this.optionHierarchyRepository.findOneById(id);
            while (!container.getLabel().equals(Constants.CHAPTER_NAME)) {
                container = this.optionHierarchyRepository.findOneById(container.getParentId());
            }
            return container.getId();
        }
        return -1L;
    }

    /**
     * Returns the existence a container with the given ID in the database
     *
     * @param id unique id of container object
     * @return existence of container in database
     */
    public boolean containerExistsDB(Long id) {
        return this.optionHierarchyRepository.existsById(id);
    }

    /**
     * Returns the existence a container with the given ID in the cache
     *
     * @param id unique id of container object
     * @param base cache of an AOP
     * @return existence of container in cache
     */
    public boolean containerExistsCache(Long id, AOGReturn base) {
        for (ReturnChapter chapter : base.getResults()) {
            // chapter container check
            if (chapter.getChapterId().equals(id)) {
                return true;
            }
            for (ReturnSubChapter subChapter: chapter.getSubChapterList()) {
                // subchapter container check
                if (subChapter.getSubChapterId().equals(id)) {
                    return true;
                }
                for (ReturnCategory category: subChapter.getCategories()) {
                    // category container check
                    if (category.getCategoryId().equals(id)) {
                        return true;
                    }
                }
            }
            for (ReturnCategory category: chapter.getCategories()) {
                // category container check
                if (category.getCategoryId().equals(id)) {
                    return true;
                }
            }
        }
        // container chapter does not exist
        return false;
    }

    /**
     * <p>Searches the AOG for the specified term or phrase</p>
     *
     * @param term the term or phrase that is searched for in the doc
     * @param base the AOG the search will be enacted upon
     * @return The chapters with matching categories, sub-chapters, overviews, etc
     */
    public AOGReturn searchAOG(String term, AOGReturn base) {
        AOGReturn aogReturn = new AOGReturn();
        List<ReturnChapter> chapters = base.getResults();
        List<ReturnChapter> returnChapters = new ArrayList<>();

        //Use a regex pattern to search for case insensitive string
        Pattern searchTerm = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
        Matcher searchMatch;
        // Checking categories inside chapter
        chapters.stream().map(chapterBase -> {
            ReturnChapter chapter = new ReturnChapter(chapterBase);

            if (chapter.getSubChapterList() != null && chapter.getSubChapterList().size() > 0) {
                chapter.getSubChapterList().stream().map(subChapter -> {

                    if (subChapter.getCategories() != null && subChapter.getCategories().size() > 0) {
                        // Checking categories
                        boolean flag = searchCategories(subChapter.getCategories(), searchTerm);
                        if (!flag) {
                            flag = searchOverviewAndDefinitionByCategory(subChapter.getCategories(), searchTerm);
                        }
                        subChapter.setMatch(flag);
                    }
                    if (!subChapter.isMatch()) {
                        Matcher searchMatchName = searchTerm.matcher(subChapter.getSubChapterName().trim());
                        if (searchMatchName.find()) {
                            // Checking subchapter name
                            subChapter.setMatch(true);
                        }
                    }
                    if (subChapter.isMatch()) {
                        chapter.setMatch(subChapter.isMatch());
                    }
                    return subChapter.isMatch();
                }).collect(Collectors.toList());
            } else {
                if (chapter.getCategories() != null && chapter.getCategories().size() > 0) {
                    boolean flag = searchCategories(chapter.getCategories(), searchTerm);
                    if (!flag) {
                        flag = searchOverviewAndDefinitionByCategory(chapter.getCategories(), searchTerm);
                    }
                    chapter.setMatch(flag);
                }
            }
            // Checking definitions inside chapter
            if (!chapter.isMatch()) {
                chapter.getDefinitions().stream().map(definition -> {
                    if (checkOverviewAndDefinition(definition.getText(), searchTerm)) {
                        chapter.setMatch(true);
                    }
                    return definition.getText();
                }).collect(Collectors.toList());
            }
            // Checking overviews inside chapter
            if (!chapter.isMatch()) {
                chapter.getOverviews().stream().map(overview -> {
                    if (checkOverviewAndDefinition(overview.getText(), searchTerm)) {
                        chapter.setMatch(true);
                    }
                    return overview.getText();
                }).collect(Collectors.toList());
            }
            // Checking for chapter name
            if (!chapter.isMatch()) {
                Matcher searchMatchName = searchTerm.matcher(chapter.getChapterName().trim());
                if (searchMatchName.find()) {
                    chapter.setMatch(true);
                }
            }
            // if there were results chapter is added to returnChapters
            if (chapter.isMatch()) {
                returnChapters.add(chapter);
            }
            return chapter;

        }).collect(Collectors.toList());

        // the unnecessary selections for categories, chapters and subchapters are removed
        for (ReturnChapter listReturnChapters : returnChapters) {
            listReturnChapters.getCategories().removeIf(categorie -> !categorie.isMatch());
            listReturnChapters.getSubChapterList().removeIf(subChapther -> !subChapther.isMatch());
        }
        returnChapters.removeIf(chapter -> !chapter.isMatch());
        for (ReturnChapter listReturnChapters : returnChapters) {
            for (ReturnSubChapter listReturnSubChapters : listReturnChapters.getSubChapterList()) {
                listReturnSubChapters.getCategories().removeIf(categorie -> !categorie.isMatch());
            }
        }
        aogReturn.setResults(returnChapters);
        return aogReturn;
    }

    /**
     * Searches for only the chapters sent in through the request
     *
     * @param chapters A list of chapter id's
     * @param base     The AOG cache that we are looking for
     * @return Returns a new AOGReturn object containing only the chapters requested
     */
    public AOGReturn getSearchChapters(List<Long> chapters, AOGReturn base) {
        // check empty list of chapters
        if (chapters.size() == 0) {
            return base;
        }

        AOGReturn theReturn = new AOGReturn();

        List<ReturnChapter> baseChapters = base.getResults();
        List<ReturnChapter> results = new ArrayList<>();

        for (Long chapter : chapters) {
            for (ReturnChapter aog : baseChapters) {
                if (aog.getChapterId().equals(chapter)) {
                    results.add(aog);
                    break;
                }
            }
        }

        theReturn.setResults(results);

        return theReturn;

    }

    /**
     * Searches for only the chapters sent in through the request
     *
     * @param chapter A list of chapter id's
     * @param base    The AOG cache that we are looking for
     * @return Returns a new AOGReturn object containing only the chapters requested
     */
    public AOGReturn getSearchChapter(Long chapter, AOGReturn base) {
        AOGReturn theReturn = new AOGReturn();

        List<ReturnChapter> baseChapters = base.getResults();
        List<ReturnChapter> results = new ArrayList<>();

        for (ReturnChapter aog : baseChapters) {
            if (aog.getChapterId().equals(chapter)) {
                results.add(aog);
                break;
            } //end if
        } //end foreach

        theReturn.setResults(results);

        return theReturn;

    }//end getSearchChapters


    /**
     * <p>Private method to check overviews and definitions for a match</p>
     *
     * @param def  the string that the search will be enacted upon
     * @param term the regex pattern that will be searched for (case insensitive)
     * @return a boolean value indicating whether the def was found
     */
    private boolean checkOverviewAndDefinition(String def, Pattern term) {
        Matcher matcher;
        if (def != null) {
            matcher = term.matcher(def);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Private method to search through a list of categories and return whether any
     * matched</p>
     *
     * @param categories A list of categories to be searched through
     * @param term       A regex pattern that will be searched for in the categories
     * @return A boolean value that indicates whether the term was found in any of the
     * categories
     */
    private boolean searchCategories(List<ReturnCategory> categories, Pattern term) {
        boolean catMatch = false;

        Matcher matcher;

        //loop through the categories
        for (ReturnCategory category : categories) {
            matcher = term.matcher(category.getCategoryName().trim());
            if (matcher.find()) {
                category.setMatch(true);
                catMatch = true;
                continue;
            }
            if (category.getStandard() != null) {
                matcher = term.matcher(category.getStandard());
                if (matcher.find()) {
                    category.setMatch(true);
                    catMatch = true;
                    continue;
                }
            }
            if (category.getBar() != null) {
                matcher = term.matcher(category.getBar());
                if (matcher.find()) {
                    category.setMatch(true);
                    catMatch = true;
                    continue;
                }
            }
            if (category.getNonstandard() != null) {
                matcher = term.matcher(category.getNonstandard());
                if (matcher.find()) {
                    category.setMatch(true);
                    catMatch = true;
                    continue;
                }
            }
            if (category.getNoExceptions() != null) {
                matcher = term.matcher(category.getNoExceptions());
                if (matcher.find()) {
                    category.setMatch(true);
                    catMatch = true;
                    continue;
                }
            }
            if (category.getAdditionalInformation() != null) {
                matcher = term.matcher(category.getAdditionalInformation());
                if (matcher.find()) {
                    category.setMatch(true);
                    catMatch = true;
                }
            }
        }
        return catMatch;
    }
    private boolean searchOverviewAndDefinitionByCategory(List<ReturnCategory> categories, Pattern term) {
        boolean catMatch = false;
        Matcher matcher;
        //loop through the categories
        for (ReturnCategory category : categories) {
            // Checking definitions inside category
            if (category.getDefinitions() != null) {
                category.getDefinitions().stream().map(definition -> {
                    if (checkOverviewAndDefinition(definition.getText(), term)) {
                        category.setMatch(true);
                    }
                    return definition.getText();
                }).collect(Collectors.toList());
                catMatch = category.isMatch();
            }
            if (!catMatch) {
                if (category.getOverviews() != null) {
                    category.getOverviews().stream().map(overview -> {
                        if (checkOverviewAndDefinition(overview.getText(), term)) {
                            category.setMatch(true);
                        }
                        return overview.getText();
                    }).collect(Collectors.toList());
                    catMatch = category.isMatch();
                }
            }
        }
        return catMatch;
    }

    /**
     * <p>Pulls the AOG from the database, assembles it and caches it in memory</p>
     *
     * @param id The ID of the AOG to be pulled from the database
     * @return Returns the AOG, if the pull was successful
     */
    @CachePut
    public AOGReturn instantiateAOG(Long id) {
        LOGGER.info(String.format("Instantiating AOG Cache with id = %d", id));
        long startTime = System.nanoTime();
        AOGReturn result = new AOGReturn();

        List<ReturnChapter> results = new ArrayList<>();

        //Get all of the Chapters
        List<OptionHierarchy> chaps = this.optionHierarchyRepository
                .findByParentIdAndLabel(id, Constants.CHAPTER_NAME);
        Collections.sort(chaps);

        //iterate through the chapters
        for (OptionHierarchy chap : chaps) {
            //Create a new chapter
            ReturnChapter chapter = new ReturnChapter(chap.getLabelTxt(), chap.getId(), chap.getSeqNbr());

            List<Definition> definitions = new ArrayList<>();
            List<Overview> overviews = new ArrayList<>();

            //Get the overview and definition(if they exist)
            setDefsAndOverViews(definitions, null, overviews, chap.getId());

            //get the subChapters(if they exist)
            List<OptionHierarchy> subs = this.optionHierarchyRepository
                    .findByParentIdAndLabel(chap.getId(), Constants.SUBCHAPTER_NAME);
            Collections.sort(subs);

            // create as empty instead of null
            List<ReturnSubChapter> subChapters = new ArrayList<>();

            if (subs.size() > 0) {

                //Iterate through the sub-chapters
                for (OptionHierarchy sub : subs) {

                    //Create a new sub-chapter object
                    ReturnSubChapter subChapter = new ReturnSubChapter(sub.getLabelTxt(), sub.getId(), sub.getSeqNbr());

                    List<Definition> subDefs = new ArrayList<>();
                    setDefsAndOverViews(definitions, subDefs, overviews, sub.getId());
                    subChapter.setCategories(getCategories(chap, sub));

                    subChapters.add(subChapter);
                }
            } else {
                chapter.setCategories(getCategories(chap, null));
            }
            chapter.setSubChapterList(subChapters);
            chapter.setDefinitions(definitions);
            chapter.setOverviews(overviews);

            //add the definitions and overviews to the categories
            setDefsAndOverviewsCategory(chapter);

            // Get the attachments for this chapter
            setAttachmentsForChapter(chapter);

            results.add(chapter);

        }

        result.setResults(results);
        long endTime = System.nanoTime();
        LOGGER.info(String.format("Instantiating AOG Cache Completed with id = %d, and took %d seconds", id, (endTime - startTime) / 1000000000));
        return result;
    }


    public AOGReturn instantiateAOG(Long id, AOGReturn base, Long containerId) {
        LOGGER.info(String.format("Instantiating AOG Cache with id = %d for the container id = %d", id, containerId));
        long startTime = System.nanoTime();
        AOGReturn result = new AOGReturn();

        List<ReturnChapter> results = new ArrayList<>();

        //Get all of the Chapters
        List<OptionHierarchy> chaps = this.optionHierarchyRepository
                .findByParentIdAndLabel(id, Constants.CHAPTER_NAME);
        Collections.sort(chaps);

        //iterate through the chapters
        int index = 0;
        for (OptionHierarchy chap : chaps) {

            if (chap.getId().equals(containerId)) {
                //Create a new chapter
                ReturnChapter chapter = new ReturnChapter(chap.getLabelTxt(), chap.getId(), chap.getSeqNbr());

                List<Definition> definitions = new ArrayList<>();
                List<Overview> overviews = new ArrayList<>();

                //Get the overview and definition(if they exist)
                setDefsAndOverViews(definitions, null, overviews, chap.getId());

                //get the subChapters(if they exist)
                List<OptionHierarchy> subs = this.optionHierarchyRepository
                        .findByParentIdAndLabel(chap.getId(), Constants.SUBCHAPTER_NAME);
                Collections.sort(subs);

                // create as empty instead of null
                List<ReturnSubChapter> subChapters = new ArrayList<>();

                if (subs.size() > 0) {

                    //Iterate through the sub-chapters
                    for (OptionHierarchy sub : subs) {

                        //Create a new sub-chapter object
                        ReturnSubChapter subChapter = new ReturnSubChapter(sub.getLabelTxt(), sub.getId(), sub.getSeqNbr());

                        List<Definition> subDefs = new ArrayList<>();
                        setDefsAndOverViews(definitions, subDefs, overviews, sub.getId());
                        subChapter.setCategories(getCategories(chap, sub));

                        subChapters.add(subChapter);
                    }
                } else {
                    chapter.setCategories(getCategories(chap, null));
                }
                chapter.setSubChapterList(subChapters);
                chapter.setDefinitions(definitions);
                chapter.setOverviews(overviews);

                //add the definitions and overviews to the categories
                setDefsAndOverviewsCategory(chapter);

                // Get the attachments for this chapter
                setAttachmentsForChapter(chapter);

                results.add(chapter);

            } else {
                results.add(base.getResults().get(index));
            }
            index++;
        }

        result.setResults(results);
        long endTime = System.nanoTime();
        LOGGER.info(String.format("Instantiating AOG Cache Completed with id = %d, and took %d seconds", id, (endTime - startTime) / 1000000000));
        return result;
    }

    private void setDefsAndOverviewsCategory(ReturnChapter chapter) {
        if (chapter.getSubChapterList().size() > 0) {
            for (ReturnSubChapter subChapter : chapter.getSubChapterList()) {
                if (subChapter.getCategories() != null && subChapter.getCategories().size() > 0) {
                    for (ReturnCategory category : subChapter.getCategories()) {
                        category.setDefinitions(chapter.getDefinitions());
                        category.setOverviews(chapter.getOverviews());
                    }
                }
            }
        } else if (chapter.getCategories().size() > 0) {
            for (ReturnCategory category : chapter.getCategories()) {
                category.setDefinitions(chapter.getDefinitions());
                category.setOverviews(chapter.getOverviews());
            }
        }
    }

    /**
     * Sets attachments that belong to the current chapter
     *
     * @param chapter
     */
    private void setAttachmentsForChapter(ReturnChapter chapter) {
        List<Attachment> attachments = this.attachmentsRepository.findByHierarchyId(chapter.getChapterId());
        if (attachments != null && attachments.size() > 0) {
            chapter.setAttachments(attachments);
        }
    }

    /**
     * <p>Private method to set the definitions and overviews of Chapters,
     * SubChapters and Categories</p>
     *
     * @param defs      Definitions set at the chapter level
     * @param subDefs   Definitions set at the sub-chapter leve
     * @param overviews Overviews for the chapter
     * @param id        Hierarchy id used to find the overviews and definitions in the database
     */
    private void setDefsAndOverViews(List<Definition> defs, List<Definition> subDefs, List<Overview> overviews, Long id) {
        List<OptionHeader> headers = this.optionHeaderRepository.findByHierarchyId(id);
        if (headers != null && headers.size() > 0) {
            for (OptionHeader optionHeader : headers) {
                if (optionHeader != null) {
                    if (optionHeader.getDefinition() != null) {
                        defs.add(new Definition(optionHeader.getHierarchyId(), cleanString(optionHeader.getDefinition())));
                    }
                    if (optionHeader.getOverview() != null) {
                        overviews.add(new Overview(optionHeader.getHierarchyId(), cleanString(optionHeader.getOverview())));
                    }

                    if (subDefs != null) {
                        if (optionHeader.getDefinition() != null) {
                            subDefs.add(new Definition(optionHeader.getHierarchyId(), cleanString(optionHeader.getDefinition())));
                        }
                    }
                }
            }
        }
    }

    private String cleanString(String text) {
        text = text.replace("<span>Ctrl + Click to open in a new tab</span>", "");
        text = text.replace("Ctrl + Click to open in a new tab", "");
        text = text.replace(" .", ".");
        text = text.replace("contenteditable=\"false\"","");
        text = text.replace("<p><br></p>","");
        return text;
    }

    /**
     * <p>Returns a list of categories that will be added to either the
     * chapter or sub-chapter. Both parameters cannot be null</p>
     *
     * @param chapter    The chapter to add the categories to (can be null)
     * @param subChapter The sub-chapter to add the categories to (can be null)
     * @return Returns a list of instantiated and populated category objects
     */
    private List<ReturnCategory> getCategories(OptionHierarchy chapter, OptionHierarchy subChapter) {

        final String title = Constants.CATEGORY_NAME;
        List<OptionHierarchy> cats;

        List<ReturnCategory> categories = new ArrayList<>();

        if (subChapter != null) {
            cats = this.optionHierarchyRepository.findByParentIdAndLabel(subChapter.getId(), title);
        } else {
            cats = this.optionHierarchyRepository.findByParentIdAndLabel(chapter.getId(), title);
        }

        //Create category list
        if (cats.size() > 0) {
            Collections.sort(cats);

            //iterate through the categories and create the objects
            for (OptionHierarchy cat : cats) {
                ReturnCategory category = new ReturnCategory(cat.getLabelTxt(), cat.getId(), cat.getSeqNbr());

                OptionCategoryNA optionCategoryNA = this.optionCategoryNARepository
                        .findByHierarchyId(cat.getId());

                category.setChapterName(chapter.getLabelTxt());
                if (optionCategoryNA.getBarApproval()!=null)
                    category.setBar(cleanString(optionCategoryNA.getBarApproval()));
                if (optionCategoryNA.getStandard()!=null)
                    category.setStandard(cleanString(optionCategoryNA.getStandard()));
                if (optionCategoryNA.getNonStandard()!=null)
                    category.setNonstandard(cleanString(optionCategoryNA.getNonStandard()));
                if (optionCategoryNA.getExcluded()!=null)
                    category.setNoExceptions(cleanString(optionCategoryNA.getExcluded()));
                if (optionCategoryNA.getAdditionalInformation()!=null)
                    category.setAdditionalInformation(cleanString(optionCategoryNA.getAdditionalInformation()));

                List<Attachment> attachments = this.attachmentsRepository
                        .findByHierarchyId(cat.getId());

                if (attachments != null && attachments.size() > 0) {
                    category.setAttachments(attachments);
                }

                if (subChapter != null) {
                    category.setSubChapter(subChapter.getLabelTxt());
                }

                //get owner and last_update info
                //***This may need to be changed based on how the reviews are stored in DB***
                OptionReview optionReview = this.optionReviewRepository.findByHierarchyId(cat.getId());

                if (optionReview != null) {
                    category.setOwner(optionReview.getApproverId());
                    category.setLastUpdated(optionReview.getReviewDate());
                }

                categories.add(category);
            }
        }

        return categories;
    }


    /**
     * Deletes a container from the database if it contains no children
     *
     * @param id hierarchyId of the item to be deleted
     * @return success of delete from Database
     */
    public boolean deleteFromDatabase(Long id) {

        if (this.optionHierarchyRepository.existsById(id)) {
            OptionHierarchy hierarchy = this.optionHierarchyRepository.findOneById(id);
            // check for children
            List<OptionHierarchy> subChapterChildren =
                    this.optionHierarchyRepository.findByParentIdAndLabel(id, Constants.SUBCHAPTER_NAME);
            List<OptionHierarchy> categoryChildren =
                    this.optionHierarchyRepository.findByParentIdAndLabel(id, Constants.CATEGORY_NAME);
            // delete child containers
            if (subChapterChildren.size() > 0 || categoryChildren.size() > 0) {
                throw new UpdateFailureException();
            }

            // delete related tables
            if (hierarchy.getLabel().equals(Constants.CHAPTER_NAME)) {
                this.optionHeaderRepository.deleteById(id);
            } else if (hierarchy.getLabel().equals(Constants.CATEGORY_NAME)) {
                this.optionCategoryNARepository.deleteById(id);
            }

            // delete actual container now that dependencies are gone
            this.optionHierarchyRepository.deleteById(id);
            LOGGER.info(String.format("[DBPullService] - Deleted Container with id=%d from Database", id));
        } else {
            LOGGER.info(String.format("[DBPullService] - Container with id=%d not in Database", id));
        }

        // potential for exceptions which will be handled in calling end point
        return true;

    }

    public <T> void addToList(List<OptionHierarchy> target, Stream<OptionHierarchy> source) {
        source.forEachOrdered(target::add);
    }

    public <T> void addToCategories(List<ReturnCategory> target, Stream<ReturnCategory> source) {
        source.forEachOrdered(target::add);
    }

    public <T> void addToSubChapter(List<ReturnSubChapter> target, Stream<ReturnSubChapter> source) {
        source.forEachOrdered(target::add);
    }

    public <T> void addToChapter(List<ReturnChapter> target, Stream<ReturnChapter> source) {
        source.forEachOrdered(target::add);
    }

    public List<ReturnCategory> getCatagoriesByIds(AOGReturn base,Long id,List<Long> cats) {

        List<ReturnCategory> filteredCategoriesList = null;
        if (this.optionHierarchyRepository.existsById(id)) {
            filteredCategoriesList = new ArrayList<>();
            OptionHierarchy hierarchy = this.optionHierarchyRepository.findOneById(id);
            ReturnChapter chapter = base.getResults().stream().filter(c -> c.getChapterId().equals(id)).findAny()
                    .orElse(null);
            //iterate through the categories and create the objects
            if (chapter.getSubChapterList().size() > 0) {
                for (ReturnSubChapter subChapter : chapter.getSubChapterList()) {
                    if (subChapter.getCategories() != null && subChapter.getCategories().size() > 0) {
                        List<ReturnCategory> filteredCategoriesSub = subChapter.getCategories().stream()
                                .filter(category -> cats.stream()
                                        .anyMatch(cat ->
                                                category.getCategoryId().equals(cat)))
                                .collect(Collectors.toList());
                        addToCategories(filteredCategoriesList, filteredCategoriesSub.stream());
                    }
                }
            } else if (chapter.getCategories().size() > 0) {
                filteredCategoriesList = chapter.getCategories().stream()
                        .filter(category -> cats.stream()
                                .anyMatch(cat ->
                                        category.getCategoryId().equals(cat)))
                        .collect(Collectors.toList());
            }
            // potential for exceptions which will be handled in calling end point
        }
        return filteredCategoriesList;
    }

    public List<ReturnSubChapter> getSubChaptersByIds(AOGReturn base,List<Long> subs) {

        List<ReturnChapter> chapters = base.getResults();
        List<ReturnSubChapter> subChapterList = new ArrayList<>();

        chapters.forEach(chapter -> {
            if (chapter.getSubChapterList().size() > 0) {

                List<ReturnSubChapter> filteredSubChapterList = chapter.getSubChapterList().stream()
                        .filter(subChapter -> subs.stream()
                                .anyMatch(sub ->
                                        subChapter.getSubChapterId().equals(sub)))
                        .collect(Collectors.toList());
                addToSubChapter(subChapterList,filteredSubChapterList.stream());
            }
            // potential for exceptions which will be handled in calling end point
        });
        return subChapterList;
    }

    public List<ReturnChapter> getChaptersByIds(AOGReturn base,List<Long> chaps) {

        List<ReturnChapter> chapters = base.getResults();

        List<ReturnChapter> filteredChapterList = chapters.stream()
                        .filter(chapter -> chaps.stream()
                                .anyMatch(chap ->
                                        chapter.getChapterId().equals(chap)))
                        .collect(Collectors.toList());

        return filteredChapterList;
    }



    public boolean concatCatagories(Long id) {

        if (this.optionHierarchyRepository.existsById(id)) {
            OptionHierarchy hierarchy = this.optionHierarchyRepository.findOneById(id);
            // check for children
            List<OptionHierarchy> categoryChildren =
                    this.optionHierarchyRepository.findByParentIdAndLabel(id, Constants.CATEGORY_NAME);

            List<OptionHierarchy> subChapterChildren =
                    this.optionHierarchyRepository.findByParentIdAndLabel(id, Constants.SUBCHAPTER_NAME);
            for (OptionHierarchy optionHierarchy: subChapterChildren) {
                List<OptionHierarchy> categoryChildrenOfSubChapters =
                        this.optionHierarchyRepository.findByParentIdAndLabel(optionHierarchy.getId(), Constants.CATEGORY_NAME);
                addToList(categoryChildren, categoryChildrenOfSubChapters.stream());
            }

            for (OptionHierarchy hierarchyCategories : categoryChildren) {
                hierarchyCategories.setParentId(id);
            }

            this.optionHierarchyRepository.saveAll( categoryChildren );

            for (OptionHierarchy optionHierarchy: subChapterChildren) {
                deleteFromDatabase(optionHierarchy.getId());
            }

            LOGGER.info(String.format("[DBPullService] - Change Container Type with id=%d from Database", id));
        } else {
            LOGGER.info(String.format("[DBPullService] - Container with id=%d not in Database", id));
        }
        // potential for exceptions which will be handled in calling end point
        return true;

    }

    public long updateToDatabase(Long id, AOGReturn base, Long aogId) {

        OptionHierarchy saved = null;

        if (this.optionHierarchyRepository.existsById(id)) {
            OptionHierarchy hierarchy = this.optionHierarchyRepository.findOneById(id);


            // check for children
            List<OptionHierarchy> categoryChildren =
                    this.optionHierarchyRepository.findByParentIdAndLabel(id, Constants.CATEGORY_NAME);

            Long betweenPos = getChapterPosition(aogId, hierarchy.getLabelTxt(), base);

            OptionHierarchy hierarchyUpdate = new OptionHierarchy(hierarchy.getParentId(), Constants.CHAPTER_NAME, hierarchy.getLabelTxt(), betweenPos);
            hierarchyUpdate.setId(id);
            saved = this.optionHierarchyRepository.saveAndFlush(hierarchyUpdate);

            OptionHeader header = new OptionHeader(saved.getId());
            this.optionHeaderRepository.save(header);

            // save blank
            ContainerCreate blank = new ContainerCreate();
            blank.setContainerLabel(Constants.SUBCHAPTER_NAME);
            blank.setParentId(id);
            blank.setContainerName(String.format("[Insert %s Name]", blank.getContainerLabel()));
            //Long savedId = addToDatabase(blank, base, Constants.SEQUENCE_GAP);
            Long savedId = addToDatabase(blank, base, betweenPos);

            for (OptionHierarchy hierarchyCategories : categoryChildren) {
                hierarchyCategories.setParentId(savedId);
            }

            this.optionHierarchyRepository.saveAll( categoryChildren );
        }

        // potential for exceptions which will be handled in calling end point
        return saved.getId();
    }


    public long addToDatabase(ContainerCreate container, AOGReturn base) {
        // validate container label
        long betweenPos;
        switch (container.getContainerLabel()) {
            case Constants.CHAPTER_NAME:
                betweenPos = getChapterPosition(container.getAogId(), container.getContainerName(), null, base);
                break;
            case Constants.SUBCHAPTER_NAME:
            case Constants.CATEGORY_NAME:
                betweenPos = getPosition(container, base);
                break;
            default:
                throw new UpdateFailureException();
        }

        container.setSeqNumber(betweenPos);

        OptionHierarchy hierarchy = new OptionHierarchy(container);
        OptionHierarchy saved = this.optionHierarchyRepository.saveAndFlush(hierarchy);

        container.setHierarchyId(saved.getId());

        // add to related table
        if (container.getContainerLabel().equals(Constants.CATEGORY_NAME)) {
            OptionCategoryNA category = new OptionCategoryNA(container.getHierarchyId());
            this.optionCategoryNARepository.save(category);
        } else if (container.getContainerLabel().equals(Constants.CHAPTER_NAME)) {
            OptionHeader header = new OptionHeader(container.getHierarchyId());
            this.optionHeaderRepository.save(header);

            // save blank
            ContainerCreate blank = new ContainerCreate();
            blank.setContainerLabel(container.hasSubChapters() ? Constants.SUBCHAPTER_NAME : Constants.CATEGORY_NAME);
            blank.setParentId(container.getHierarchyId());
            blank.setContainerName(String.format("[Insert %s Name]", blank.getContainerLabel()));
            addToDatabase(blank, base);
        }

        // potential for exceptions which will be handled in calling end point
        return saved.getId();
    }


    public long addToDatabase(ContainerCreate container, AOGReturn base, long succecsorPos) {
        // validate container label
        long betweenPos = succecsorPos;

        container.setSeqNumber(betweenPos);

        OptionHierarchy hierarchy = new OptionHierarchy(container);
        OptionHierarchy saved = this.optionHierarchyRepository.saveAndFlush(hierarchy);

        container.setHierarchyId(saved.getId());

        // add to related table
        if (container.getContainerLabel().equals(Constants.CATEGORY_NAME)) {
            OptionCategoryNA category = new OptionCategoryNA(container.getHierarchyId());
            this.optionCategoryNARepository.save(category);
        } else if (container.getContainerLabel().equals(Constants.CHAPTER_NAME)) {
            OptionHeader header = new OptionHeader(container.getHierarchyId());
            this.optionHeaderRepository.save(header);

            // save blank
            ContainerCreate blank = new ContainerCreate();
            blank.setContainerLabel(container.hasSubChapters() ? Constants.SUBCHAPTER_NAME : Constants.CATEGORY_NAME);
            blank.setParentId(container.getHierarchyId());
            blank.setContainerName(String.format("[Insert %s Name]", blank.getContainerLabel()));
            addToDatabase(blank, base);
        }

        // potential for exceptions which will be handled in calling end point
        return saved.getId();
    }

    /**
     * Find what the position of a new Chapter should be based on alphabetical order.
     *
     * @param aogId       AOG id being updated
     * @param chapterName Name of the chapter being inserted
     * @param base        Cached AOG that may be re-sequenced
     * @return
     */
    private Long getChapterPosition(Long aogId, String chapterName, AOGReturn base) {
        List<OptionHierarchy> aog = this.optionHierarchyRepository
                .findByParentIdAndLabel(aogId, Constants.CHAPTER_NAME);
        Collections.sort(aog);
        OptionHierarchy oldChapter = null;

        long predecessorPos = 0;
        long successorPos = aog.get(aog.size() - 1).getSeqNbr() + 2 * Constants.SEQUENCE_GAP;

        for (OptionHierarchy chapter : aog) {
            if (oldChapter != null && oldChapter == chapter) {
                continue;
            }
            if (chapter.getLabelTxt().compareToIgnoreCase(chapterName) < 0) {
                predecessorPos = chapter.getSeqNbr();
            } else if (chapter.getLabelTxt().compareToIgnoreCase(chapterName) > 0) {
                successorPos = chapter.getSeqNbr();
                break;
            }
        }
        // check closeness and retry
        if (successorPos - predecessorPos == 1) {
            reposition(aogId, Constants.CHAPTER_NAME);
            repositionCache(aogId, aogId, base);
            return getChapterPosition(aogId, chapterName, base);
        }

        return (successorPos - predecessorPos) / 2 + predecessorPos;
    }


    private Long getChapterPosition(Long aogId, String chapterName, OptionHierarchy oldChapter, AOGReturn base) {
        List<OptionHierarchy> aog = this.optionHierarchyRepository
                .findByParentIdAndLabel(aogId, Constants.CHAPTER_NAME);
        Collections.sort(aog);

        long predecessorPos = 0;
        long successorPos = aog.get(aog.size() - 1).getSeqNbr() + 2 * Constants.SEQUENCE_GAP;

        for (OptionHierarchy chapter : aog) {
            if (oldChapter != null && oldChapter == chapter) {
                continue;
            }
            if (chapter.getLabelTxt().compareToIgnoreCase(chapterName) < 0) {
                predecessorPos = chapter.getSeqNbr();
            } else if (chapter.getLabelTxt().compareToIgnoreCase(chapterName) > 0) {
                successorPos = chapter.getSeqNbr();
                break;
            } else {
                throw new UpdateFailureException();
            }
        }
        // check closeness and retry
        if (successorPos - predecessorPos == 1) {
            reposition(aogId, Constants.CHAPTER_NAME);
            repositionCache(aogId, aogId, base);
            return getChapterPosition(aogId, chapterName, oldChapter, base);
        }

        return (successorPos - predecessorPos) / 2 + predecessorPos;
    }

    /**
     * Finds what the position of a new container should be. The sibling containers may be
     * shifted to make room for it.
     *
     * @param container ContainerCreate object with information for new container
     * @param base      Cached AOG that may be re-sequenced
     * @return the sequence number between the two siblings
     */
    private Long getPosition(ContainerCreate container, AOGReturn base) {
        Long parentId = container.getParentId();
        Long predecessorId = container.getPredecessorId();
        Long successorId = container.getSuccessorId();
        long predecessorPos = 0;
        long successorPos = Constants.SEQUENCE_GAP;

        // check for siblings
        List<OptionHierarchy> children = this.optionHierarchyRepository
                .findByParentIdAndLabel(parentId, container.getContainerLabel());
        Collections.sort(children);

        // get actual positions
        if (predecessorId != null) {
            OptionHierarchy predecessor = this.optionHierarchyRepository.findOneById(predecessorId);
            predecessorPos = predecessor.getSeqNbr();
        }
        if (successorId != null) {
            OptionHierarchy successor = this.optionHierarchyRepository.findOneById(successorId);
            successorPos = successor.getSeqNbr();
        }

        // check closeness and retry
        if (successorPos - predecessorPos == 1) {
            reposition(container.getParentId(), container.getContainerLabel());
            repositionCache(container.getAogId(), container.getParentId(), base);
            return getPosition(container, base);
        }

        if (successorId == null && predecessorId == null) {
            // validate no children exist
            if (children.size() > 0) {
                LOGGER.error("Can Not Create 1st element when other elements exist");
                LOGGER.error(String.format("%s with id=%d already exists in parent with id=%d",
                        children.get(0).getLabelTxt(), children.get(0).getId(), parentId));
                throw new UpdateFailureException();
            }
            // no siblings
            return Constants.SEQUENCE_GAP;
        } else if (successorId == null) {
            // validate no children exist after predecessor
            for (OptionHierarchy child : children) {
                if (predecessorPos < child.getSeqNbr()) {
                    LOGGER.error("Can Not Append to element that is not the last");
                    LOGGER.error(String.format("%s with id=%d comes after predecesor with id=%d",
                            child.getLabelTxt(), child.getId(), predecessorId));
                    throw new UpdateFailureException();
                }
            }
            // append to end
            return predecessorPos + Constants.SEQUENCE_GAP;
        } else if (predecessorId == null) {
            // validate no children exist before successor
            for (OptionHierarchy child : children) {
                if (child.getSeqNbr() < successorPos) {
                    LOGGER.error("Can Not Prepend to element that is not the first");
                    LOGGER.error(String.format("%s with id=%d comes before successor with id=%d",
                            child.getLabelTxt(), child.getId(), successorId));
                    throw new UpdateFailureException();
                }
            }
            // append to start
            return successorPos / 2;
        } else {
            // validate no children between predecessor and successor
            for (OptionHierarchy child : children) {
                if (predecessorPos < child.getSeqNbr() && child.getSeqNbr() < successorPos) {
                    LOGGER.error("Can Not Insert between non adjacent elements");
                    LOGGER.error(String.format("%s with id=%d already exists between id=%d and id=%d",
                            child.getLabelTxt(), child.getId(), predecessorId, successorId));
                    throw new UpdateFailureException();
                }
            }
            // insert in the middle
            return (successorPos - predecessorPos) / 2 + predecessorPos;
        }
    }

    /**
     * Reassign the positions of child containers of the parent at set intervals for the Database
     *
     * @param parentId       parent container Id
     * @param containerLabel label for type of container being re-sequenced
     */
    private void reposition(Long parentId, String containerLabel) {
        // get database list
        List<OptionHierarchy> children = this.optionHierarchyRepository
                .findByParentIdAndLabel(parentId, containerLabel);
        Collections.sort(children);

        for (int pos = 1; pos <= children.size(); pos++) {
            OptionHierarchy child = children.get(pos - 1);
            child.setSeqNbr(pos * Constants.SEQUENCE_GAP);
        }
        this.optionHierarchyRepository.saveAll(children);
    }

    /**
     * Reassign the positions of child containers of the parent at set intervals for the cache
     *
     * @param id       AOG id being updated in the cache
     * @param parentId parent container Id
     * @param base     Cached AOG to be re-sequenced
     * @return The updated AOG object
     */
    @CachePut(key = "#id")
    public AOGReturn repositionCache(Long id, Long parentId, AOGReturn base) {
        // get cached
        List<ReturnChapter> chapters = base.getResults();

        // update chapters positions
        if (id.equals(parentId)) {
            for (int pos = 1; pos <= chapters.size(); pos++) {
                ReturnChapter chapter = chapters.get(pos - 1);
                chapter.setSeqNumber(pos * Constants.SEQUENCE_GAP);
            }
        } else {
            chapters.forEach(chapter -> {
                List<ReturnSubChapter> subChapters = chapter.getSubChapterList();
                // update chapter children positions
                if (chapter.getChapterId().equals(parentId)) {
                    // update subChapter positions
                    for (int pos = 1; pos <= subChapters.size(); pos++) {
                        ReturnSubChapter subChapter = subChapters.get(pos - 1);
                        subChapter.setSeqNumber(pos * Constants.SEQUENCE_GAP);
                    }
                    // update category positions
                    List<ReturnCategory> categories = chapter.getCategories();
                    for (int pos = 1; pos <= categories.size(); pos++) {
                        ReturnCategory category = categories.get(pos - 1);
                        category.setSeqNumber(pos * Constants.SEQUENCE_GAP);
                    }
                } else {
                    subChapters.forEach(subChapter -> {
                        // update subChapter category positions
                        if (subChapter.getSubChapterId().equals(parentId)) {
                            List<ReturnCategory> categories = subChapter.getCategories();
                            for (int pos = 1; pos <= categories.size(); pos++) {
                                ReturnCategory category = categories.get(pos - 1);
                                category.setSeqNumber(pos * Constants.SEQUENCE_GAP);
                            }
                        }
                    });
                }
            });
        }
        // save updates
        AOGReturn aogReturn = new AOGReturn();
        aogReturn.setResults(chapters);
        return aogReturn;
    }

    /**
     * Updates the Chapter in the cache
     *
     * @param id      AOG id as stored in the DB
     * @param base    The AOG to be searched for the chapter that needs to be updated
     * @param chapter The chapter that needs to be updated
     * @return AOG sent back for updating the cache
     */
    @CachePut(key = "#id")
    public AOGReturn updateCache(Long id, AOGReturn base, ReturnChapter chapter) {
        // update with tooltips
        for (Definition definition : chapter.getDefinitions()) {
            definition.setText(ProcessingUtility.addToolTip(definition.getText()));
        }
        for (Overview overview : chapter.getOverviews()) {
            overview.setText(ProcessingUtility.addToolTip(overview.getText()));
        }
        // remove match status from replacing chapter and its children
        chapter.setMatch(false);
        // update category content
        if (chapter.getSubChapterList().size() > 0) {
            for (ReturnSubChapter subChapter : chapter.getSubChapterList()) {
                subChapter.setMatch(false);
                for (ReturnCategory category : subChapter.getCategories()) {
                    category.setMatch(false);
                    addCategoryTooltips(category);
                }
            }
        } else {
            for (ReturnCategory category : chapter.getCategories()) {
                category.setMatch(false);
                addCategoryTooltips(category);
            }
        }

        List<ReturnChapter> searchAOG = base.getResults();

        //Use a for loop to iterate through the chapters to find a match
        for (ReturnChapter returnChapter : searchAOG) {
            if (returnChapter.getChapterId().equals(chapter.getChapterId())) {
                if (!returnChapter.getChapterName().equals(chapter.getChapterName())) {
                    chapter.setSeqNumber(this.optionHierarchyRepository
                            .findOneById(chapter.getChapterId()).getSeqNbr());
                    // update sequencing
                    base.removeChapter(returnChapter);
                    base.addChapter(chapter);
                    break;
                } else {
                    base.setChapter(chapter);
                    break;
                }
            }
        }

        return base;
    }

    /**
     * Updates all the content sections of the category to add tooltips for links
     *
     * @param category with title attributes added to link tags
     */
    private void addCategoryTooltips(ReturnCategory category) {
        category.setStandard(ProcessingUtility.addToolTip(category.getStandard()));
        category.setNonstandard(ProcessingUtility.addToolTip(category.getNonstandard()));
        category.setBar(ProcessingUtility.addToolTip(category.getBar()));
        category.setNoExceptions(ProcessingUtility.addToolTip(category.getNoExceptions()));
        category.setAdditionalInformation(ProcessingUtility.addToolTip(category.getAdditionalInformation()));
    }

    /**
     * Updates the Chapter in the cache
     *
     * @param id          AOG id as stored in the DB
     * @param base        The AOG to be searched for the chapter that needs to be updated
     * @param containerId The container that needs to be added
     * @return AOG sent back for updating the cache
     */
    @CachePut(key = "#id")
    public AOGReturn deleteFromCache(Long id, AOGReturn base, Long containerId) {
        List<ReturnChapter> updatedChapters = base.getResults();
        boolean found = false;

        for (ReturnChapter chapter : updatedChapters) {
            // check chapters
            if (chapter.getChapterId().equals(containerId)) {
                // validate no children
                if (chapter.getCategories().size() > 0 || chapter.getSubChapterList().size() > 0) {
                    // re-synchronize database with cache
                    updateDB(id, chapter, base);
                    throw new UpdateFailureException();
                }
                base.removeChapter(chapter);
                return base;
            } else {
                List<ReturnSubChapter> subChapters = chapter.getSubChapterList();
                // check subChapters
                for (ReturnSubChapter subChapter : subChapters) {
                    if (subChapter.getSubChapterId().equals(containerId)) {
                        if (subChapter.getCategories().size() > 0) {
                            // re-synchronize database with cache
                            updateDB(id, chapter, base);
                            throw new UpdateFailureException();
                        }
                        chapter.removeSubChapter(subChapter);
                        found = true;
                    } else {
                        List<ReturnCategory> categories = subChapter.getCategories();
                        // check subChapter categories
                        for (ReturnCategory category : categories) {
                            if (category.getCategoryId().equals(containerId)) {
                                subChapter.removeCategory(category);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                List<ReturnCategory> categories = chapter.getCategories();
                // check chapter categories
                for (ReturnCategory category : categories) {
                    if (category.getCategoryId().equals(containerId)) {
                        chapter.removeCategory(category);
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                break;
            }
        }

        AOGReturn aogReturn = new AOGReturn();
        aogReturn.setResults(updatedChapters);
        return aogReturn;
    }

    /**
     * Updates the Chapter in the cache
     *
     * @param id        AOG id as stored in the DB
     * @param base      The AOG to be searched for the chapter that needs to be updated
     * @param container ContainerCreate object with information for new container
     * @return AOG sent back for updating the cache
     */
    @CachePut(key = "#id")
    public AOGReturn addToCache(Long id, AOGReturn base, ContainerCreate container) {
        String name = container.getContainerName();
        Long parentId = container.getParentId();
        String label = container.getContainerLabel();
        Long hierarchyId = container.getHierarchyId();
        Long seqNumber = container.getSeqNumber();

        // insert container
        if (label.equals(Constants.CHAPTER_NAME)) {
            ReturnChapter chapter = new ReturnChapter(name, hierarchyId, seqNumber);

            // add blank child
            String childType = container.hasSubChapters() ? Constants.SUBCHAPTER_NAME : Constants.CATEGORY_NAME;
            OptionHierarchy child = this.optionHierarchyRepository
                    .findByParentIdAndLabel(hierarchyId, childType).get(0);
            if (container.hasSubChapters()) {
                chapter.addSubChapter(new ReturnSubChapter(
                        child.getLabelTxt(), child.getId(), child.getSeqNbr()));
            } else {
                chapter.addCategory(new ReturnCategory(
                        child.getLabelTxt(), child.getId(), child.getSeqNbr()));
            }

            base.addChapter(chapter);
            return base;
        } else {
            List<ReturnChapter> updatedChapters = base.getResults();
            boolean found = false;
            // search though Chapters
            for (ReturnChapter chapter : updatedChapters) {
                List<ReturnSubChapter> subChapters = chapter.getSubChapterList();
                // add to the chapter
                if (chapter.getChapterId().equals(parentId) && !found) {
                    if (label.equals(Constants.SUBCHAPTER_NAME)) {
                        ReturnSubChapter subChapter = new ReturnSubChapter(name, hierarchyId, seqNumber);
                        chapter.addSubChapter(subChapter);
                    } else {
                        ReturnCategory category = new ReturnCategory(
                                name, hierarchyId, seqNumber, chapter.getChapterName());
                        chapter.addCategory(category);
                    }
                    break;
                } else if (label.equals(Constants.CATEGORY_NAME) && !found) {
                    // search through subChapters only if adding category
                    for (ReturnSubChapter subChapter : subChapters) {
                        if (subChapter.getSubChapterId().equals(parentId)) {
                            ReturnCategory category = new ReturnCategory(
                                    name, hierarchyId, seqNumber, chapter.getChapterName());
                            subChapter.addCategory(category);
                            found = true;
                            break;
                        }
                    }
                }
            }
            // save and return
            AOGReturn aogReturn = new AOGReturn();
            aogReturn.setResults(updatedChapters);
            return aogReturn;
        }
    }

    @CachePut(key = "#id")
    public AOGReturn addAttachmentToCache(Long id, AOGReturn base, Attachment attachment) {
        Long containerId = attachment.getHierarchyId();
        List<ReturnChapter> updatedChapters = base.getResults();
        AOGReturn aogReturn = new AOGReturn();

        for (ReturnChapter currentChapter : updatedChapters) {
            if (currentChapter.getChapterId().equals(containerId)) {

                currentChapter.addAttachment(attachment);
            } else if (currentChapter.getSubChapterList() != null && currentChapter.getSubChapterList().size() > 0) {
                List<ReturnSubChapter> currentChapterSubChapterList = currentChapter.getSubChapterList();
                for (ReturnSubChapter subChapter : currentChapterSubChapterList) {
                    if (subChapter.getCategories() != null && subChapter.getCategories().size() > 0) {
                        addAttachmentToCategory(containerId, attachment, subChapter.getCategories());
                    }
                }
            } else if (currentChapter.getCategories() != null && currentChapter.getCategories().size() > 0) {
                addAttachmentToCategory(containerId, attachment, currentChapter.getCategories());
            }
        }

        aogReturn.setResults(updatedChapters);
        return aogReturn;
    }

    private void addAttachmentToCategory(Long containerId, Attachment attachment, List<ReturnCategory> categories) {
        for (ReturnCategory category : categories) {
            if (category.getCategoryId().equals(containerId)) {
                category.addAttachments(attachment);
                break;
            }
        }
    }

    @CachePut(key = "#id")
    public AOGReturn deleteAttachmentFromCache(Long id, AOGReturn base, AttachmentRemove attachmentRemove) {
        Long containerId = attachmentRemove.getContainerId();
        List<ReturnChapter> updatedChapters = base.getResults();
        AOGReturn aogReturn = new AOGReturn();

        for (ReturnChapter currentChapter : updatedChapters) {
            if (currentChapter.getChapterId().equals(containerId)) {
                for (Attachment attachment : currentChapter.getAttachments()) {
                    if (attachment.getId().equals(attachmentRemove.getAttachmentId())) {
                        currentChapter.removeAttachment(attachment);
                        break;
                    }
                }
            } else if (currentChapter.getSubChapterList() != null && currentChapter.getSubChapterList().size() > 0) {
                List<ReturnSubChapter> currentChapterSubChapterList = currentChapter.getSubChapterList();
                for (ReturnSubChapter subChapter : currentChapterSubChapterList) {
                    removeAttachmentFromCategory(attachmentRemove, subChapter.getCategories());
                }
            } else if (currentChapter.getCategories() != null && currentChapter.getCategories().size() > 0) {
                removeAttachmentFromCategory(attachmentRemove, currentChapter.getCategories());
            }
        }
        aogReturn.setResults(updatedChapters);
        return aogReturn;
    }

    private void removeAttachmentFromCategory(AttachmentRemove attachmentRemove, List<ReturnCategory> categories) {
        for (ReturnCategory category : categories) {
            for (Attachment attachment : category.getAttachments()) {
                if (attachment.getId().equals(attachmentRemove.getAttachmentId())) {
                    category.removeAttachments(attachment);
                    break;
                }
            }
        }
    }

    private void updateTables(List<ReturnCategory> categories, Long hierarchyId) {
        for (ReturnCategory returnCategory : categories) {
            LOGGER.info(String.format("Updating %s in %s and %s", returnCategory.getCategoryName(),
                    Constants.OPTION_HIERARCHY_TABLE, Constants.OPTION_CATEGORY_TABLE));
            // Update category hierarchy object
            OptionHierarchy categoryHierarchy = this.optionHierarchyRepository.save(
                    new OptionHierarchy(hierarchyId, returnCategory));
            // Update category content object
            addCategoryTooltips(returnCategory);
            // get new ID, in case of change
            returnCategory.setCategoryId(categoryHierarchy.getId());
            System.out.println("ID: "+hierarchyId);
            this.optionCategoryNARepository.save(new OptionCategoryNA(returnCategory));
        }
    }

    /**
     * Updates the AOG in the database
     *
     * @param id            The id of the AOG to be updated
     * @param returnChapter The chapter in the DB to be updated
     * @return returns a boolean designating success
     */
    public boolean updateDB(Long id, ReturnChapter returnChapter, AOGReturn base) {
        // Update chapter hierarchy object
        OptionHierarchy chapter = new OptionHierarchy(id, returnChapter);
        OptionHierarchy oldChapter = this.optionHierarchyRepository.findOneById(returnChapter.getChapterId());
        if (!returnChapter.getChapterName().equals(oldChapter.getLabelTxt())) {
            // update sequence number to be alphabetically ordered
            LOGGER.info("Updating Chapter Name in " + Constants.OPTION_HIERARCHY_TABLE);
            chapter.setSeqNbr(getChapterPosition(id, returnChapter.getChapterName(), oldChapter, base));
            chapter = this.optionHierarchyRepository.save(chapter);
            returnChapter.setChapterId(chapter.getId());
        }

        // Update chapter headers
        OptionHeader header = new OptionHeader(chapter.getId());

        // Update chapter definitions
        for (Definition definition : returnChapter.getDefinitions()) {
            if (definition.getText().length() > 0) {
                header.setDefinition(ProcessingUtility.addToolTip(definition.getText()));
            }
        }

        // Update chapter overviews
        for (Overview overview : returnChapter.getOverviews()) {
            if (overview.getText().length() > 0) {
                header.setOverview(ProcessingUtility.addToolTip(overview.getText()));
            }
        }

        // Update fullText
        header.setFullText(htmlToRaw(header.getOverview(), header.getDefinition()));
        LOGGER.info("Updating Chapter Header in " + Constants.OPTION_HEADER_TABLE);
        this.optionHeaderRepository.save(header);

        List<ReturnCategory> chapterCategories = returnChapter.getCategories();
        if (chapterCategories.size() > 0) {
            updateTables(chapterCategories, chapter.getId());
        } else {
            // Update subChapters
            for (ReturnSubChapter returnSubChapter : returnChapter.getSubChapterList()) {
                // Update subChapter hierarchy object
                OptionHierarchy subChapter = new OptionHierarchy(chapter.getId(), returnSubChapter);
                LOGGER.info("Updating SubChapter Name in " + Constants.OPTION_HIERARCHY_TABLE);
                subChapter = this.optionHierarchyRepository.save(subChapter);
                returnSubChapter.setSubChapterId(subChapter.getId());

                // Update subChapter categories
                List<ReturnCategory> subChapterCategories = returnSubChapter.getCategories();
                if (subChapterCategories != null) {
                    updateTables(subChapterCategories, subChapter.getId());
                }
            }
        }

        // potential for exceptions which will be handled in calling end point
        return true;
    }

    /**
     * Adds new attachment to the attachments table of the database
     *
     * @param attachmentCreate
     * @return the new attachment that was added to the db with the populated id
     */
    public Attachment addAttachmentToDb(AttachmentCreate attachmentCreate) {
        Attachment attachmentToDb = new Attachment();
        attachmentToDb.setAhref(attachmentCreate.getaHref());
        attachmentToDb.setBucketName(attachmentCreate.getBucketName());
        attachmentToDb.setFilename(attachmentCreate.getFileName());
        attachmentToDb.setHierarchyId(attachmentCreate.getContainerId());
        attachmentToDb.setTimestamp(attachmentCreate.getTimestamp());
        LOGGER.info("Adding Attachment to " + Constants.ATTACHMENTS_TABLE);
        attachmentToDb = attachmentsRepository.save(attachmentToDb);
        attachmentCreate.setAttachmentId(attachmentToDb.getId());
        return attachmentToDb;
    }

    public boolean deleteAttachmentFromDB(AttachmentRemove attachmentRemove) {
        Long attachmentId = attachmentRemove.getAttachmentId();
        boolean fileRecordExist = false;
        if (this.attachmentsRepository.existsById(attachmentId)) {
            LOGGER.info("Deleting Attachment from " + Constants.ATTACHMENTS_TABLE);
            this.attachmentsRepository.deleteById(attachmentId);
            fileRecordExist = true;
        }
        return fileRecordExist;
    }

    public List<Long> getAOGIds() {
        return this.optionHierarchyRepository.findByParentIdIsNull()
                .stream()
                .map(OptionHierarchy::getId)
                .collect(Collectors.toList());
    }
}

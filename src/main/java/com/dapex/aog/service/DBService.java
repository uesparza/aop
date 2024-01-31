package com.dapex.aog.service;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.CategoryRow;
import com.dapex.aog.dto.Chapter;
import com.dapex.aog.dto.SubChapter;
import com.dapex.aog.dto.Table;
import com.dapex.aog.exception.ParsingFailureException;
import com.dapex.aog.exception.TablePopulateFailureException;
import com.dapex.aog.jpa.domain.Attachment;
import com.dapex.aog.jpa.domain.OptionCategoryNA;
import com.dapex.aog.jpa.domain.OptionHeader;
import com.dapex.aog.jpa.domain.OptionHierarchy;
import com.dapex.aog.jpa.repo.*;
import com.dapex.aog.parser.ParseyMcParseFace;
import com.dapex.aog.utils.AOGWordDocUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.dapex.aog.utils.ProcessingUtility.htmlToRaw;
import static com.dapex.aog.utils.ProcessingUtility.removeDelimiter;

@Service
@Component
public class DBService {

    private static final Logger LOGGER = LogManager.getLogger();

    private OptionCategoryNARepository optionCategoryNARepository;

    private OptionHeaderRepository optionHeaderRepository;

    private OptionHierarchyRepository optionHierarchyRepository;

    private OptionReviewRepository optionReviewRepository;

    private AttachmentsRepository attachmentsRepository;

    private ParseyMcParseFace pmc;

    @Autowired
    DBService(AttachmentsRepository attachmentsRepository,
              OptionCategoryNARepository optionCategoryNARepository,
              OptionHeaderRepository optionHeaderRepository,
              OptionHierarchyRepository optionHierarchyRepository,
              OptionReviewRepository optionReviewRepository,
              ParseyMcParseFace parseyMcParseFace) {
        this.attachmentsRepository = attachmentsRepository;
        this.optionCategoryNARepository = optionCategoryNARepository;
        this.optionHeaderRepository = optionHeaderRepository;
        this.optionHierarchyRepository = optionHierarchyRepository;
        this.optionReviewRepository = optionReviewRepository;
        this.pmc = parseyMcParseFace;
    }

    /**
     * Take list of Chapter objects from parser, choose parent AOG hierarchy node
     * (right now hard-coded for NA, but could have switch case in future),
     * and pass them into insertChapters() function for DB insertion
     */
    public boolean populateTables(String aogLabel) {
        try {
            OptionHierarchy aogOptionHierarchy = this.optionHierarchyRepository.findOneByLabelTxt(aogLabel);
            Long aogId = aogOptionHierarchy.getId();
            String aogDocument = AOGWordDocUtility.getAOGWordDoc(aogLabel);
            String oosBucket = AOGWordDocUtility.getOOSBucketName(aogLabel);
            LocalDateTime timestamp = null;
            List<Chapter> chapters = pmc.run(aogDocument, oosBucket);
            insertChapters(chapters, aogId, oosBucket, timestamp);
            return true;
        } catch (ParsingFailureException e) {
            throw new TablePopulateFailureException();
        }
    }

    /**
     * For each chapter: insert chapter, definitions & overviews,
     * and recursively insert rows of tables and/or subchapters
     *
     * @param chapters List of Chapter objects
     * @param parentId ID of parent AOG node
     */
    private void insertChapters(List<Chapter> chapters, Long parentId, String oosBucketName, LocalDateTime timestamp) {
        long chapterSeqNbr = Constants.SEQUENCE_GAP;
        String htmlDelimiter = "<br>";
        for (Chapter chapter : chapters) {

            // Insert hierarchy object
            OptionHierarchy chapterHierarchy = new OptionHierarchy(parentId,
                    Constants.CHAPTER_NAME, chapter.getTitle(), chapterSeqNbr);
            chapterHierarchy = this.optionHierarchyRepository.save(chapterHierarchy);

            LOGGER.info("Inserted Chapter: " + chapter.getTitle());

            // Get ID of the object we just inserted
            Long chapterHierarchyId = chapterHierarchy.getId();

            // Set up Chapter Header
            OptionHeader chapterHeader = new OptionHeader(chapterHierarchyId);

            // Get Chapter Overviews
            StringBuilder fullOverview = new StringBuilder();
            List<String> overviews = chapter.getOverviews();
            insertOverviews(overviews, fullOverview, htmlDelimiter);

            // Get Chapter Definitions
            StringBuilder fullDefinition = new StringBuilder();
            Map<String, String> chapterDefinitions = chapter.getDefinitions();
            insertDefinitions(chapterDefinitions, fullDefinition, htmlDelimiter);

            // Get SubChapter Info
            List<SubChapter> subChapters = chapter.getSubChapters();
            if (subChapters != null) {
                long subChapterSeqNbr = Constants.SEQUENCE_GAP;
                for (SubChapter subChapter : subChapters) {
                    // Insert subChapter hierarchy object with parentID of the chapter object
                    OptionHierarchy subChapterHierarchy = new OptionHierarchy(
                            chapterHierarchyId, Constants.SUBCHAPTER_NAME, subChapter.getTitle(), subChapterSeqNbr);
                    subChapterHierarchy = this.optionHierarchyRepository.save(subChapterHierarchy);

                    LOGGER.info("Inserted subChapter: " + subChapter.getTitle());

                    // Get ID of the subChapter we just inserted
                    Long subChapterHierarchyId = subChapterHierarchy.getId();

                    // Get SubChapter Definitions
                    Map<String, String> subChapterDefinitions = subChapter.getDefinitions();
                    insertDefinitions(subChapterDefinitions, fullDefinition, htmlDelimiter);

                    // Tables
                    List<Table> subChapterTables = subChapter.getTables();
                    insertTables(subChapterTables, subChapterHierarchyId, oosBucketName, timestamp);
                    subChapterSeqNbr += Constants.SEQUENCE_GAP;
                }
            }

            // Remove trailing delimiters
            removeDelimiter(fullOverview, htmlDelimiter);
            removeDelimiter(fullDefinition, htmlDelimiter);

            // Start Compiling Fulltext: Unstyled Overview + Definition Content
            String fullText = htmlToRaw(fullOverview.toString(), fullDefinition.toString());

            // Insert Overview
            if (fullOverview.toString().length() > 0) {
                chapterHeader.setOverview(fullOverview.toString());
            } else {
                chapterHeader.setOverview(Constants.LEFT_BLANK);
            }
            // Insert Definition
            if (fullDefinition.toString().length() > 0) {
                chapterHeader.setDefinition(fullDefinition.toString());
            } else {
                chapterHeader.setDefinition(Constants.LEFT_BLANK);
            }
            // Insert FullText
            if (fullText.length() > 0) {
                chapterHeader.setFullText(fullText);
            }

            optionHeaderRepository.save(chapterHeader);
            LOGGER.info("Inserted chapter overview/definition: " + chapter.getTitle());

            // Insert Table Rows
            List<Table> chapterTables = chapter.getTables();
            insertTables(chapterTables, chapterHierarchyId, oosBucketName, timestamp);
            chapterSeqNbr += Constants.SEQUENCE_GAP;
        }
    }

    /**
     * Method for table rows insertion
     *
     * @param tables        chapter table or subchapter table
     * @param parentId      id of the parent container
     * @param oosBucketName bucket that attachements belong to
     */
    private void insertTables(List<Table> tables, Long parentId, String oosBucketName,LocalDateTime timestamp) {
        if (tables != null) {
            // Insert rows in each table; keep track of categorySeqNbr
            long categorySeqNbr = Constants.SEQUENCE_GAP;
            for (Table table : tables) {
                categorySeqNbr = insertTableRows(table, parentId, categorySeqNbr, oosBucketName,timestamp);
            }
        }
    }

    /**
     * Append a map of definitions to a StringBuilder
     *
     * @param definitions map of definitions
     * @param text        StringBuilder that will be updated
     * @param delimiter   string to separate entries
     */
    private void insertDefinitions(Map<String, String> definitions,
                                   StringBuilder text,
                                   String delimiter) {
        if (definitions != null) {
            for (Map.Entry<String, String> def : definitions.entrySet()) {
                if (def.getKey().length() > 0 && def.getValue().length() > 0) {
                    String definition = def.getKey().trim() + " || " + def.getValue() + delimiter;
                    text.append(definition);
                }
            }
        }
    }

    /**
     * Append a list of overviews to a StringBuilder
     *
     * @param overviews list of overviews
     * @param text      StringBuilder that will be updated
     * @param delimiter string to separate entries
     */
    private void insertOverviews(List<String> overviews,
                                 StringBuilder text,
                                 String delimiter) {
        if (overviews != null) {
            for (String overview : overviews) {
                if (overview.length() > 0) {
                    String categoryDefinition = overview + delimiter;
                    text.append(categoryDefinition);
                }
            }
        }
    }

    /**
     * Iterates through category rows and adds the category to hiearchyRepo,
     * adds the standard, nonstandard, and bar to categoryNARepo.
     * Returns a number of the final categorySequenceNumber after it is incremented,
     * as it may need to be reused for the following table under the same chapter/subChapter
     *
     * @param table          List of CategoryRows to be inserted
     * @param parentId       ID of hierarchy object PARENT
     * @param categorySeqNbr starting sequence number
     * @return new category sequence number
     */
    private long insertTableRows(Table table, Long parentId, long categorySeqNbr, String oosBucket, LocalDateTime timestamp) {
        List<CategoryRow> rows = table.getRows();
        for (CategoryRow row : rows) {
            if (!(row.getCategoryUnstyled().equals(Constants.BLANKS) &&
                    row.getStandard().equals(Constants.BLANKS) &&
                    row.getNonstandard().equals(Constants.BLANKS) &&
                    row.getBar().equals(Constants.BLANKS))) {

                OptionHierarchy categoryHierarchy = new OptionHierarchy(parentId,
                        Constants.CATEGORY_NAME, row.getCategoryUnstyled(), categorySeqNbr);
                OptionHierarchy savedCategoryHierarchy = optionHierarchyRepository.save(categoryHierarchy);
                Long categoryHierarchyId = savedCategoryHierarchy.getId();
                LOGGER.info("Inserted category row hierarchy object: " + row.getCategoryUnstyled());

                OptionCategoryNA categoryNA = new OptionCategoryNA(categoryHierarchyId, row);
                optionCategoryNARepository.save(categoryNA);
                LOGGER.info("Inserted categoryNA (standard, nonstandard, bar) for " + row.getCategoryUnstyled());

                if (row.getEmbedds() != null) {
                    List<String> embedds = row.getEmbedds();
                    if (embedds != null && embedds.size() > 0) {
                        for (String embedd : embedds) {
                            Attachment attachment = new Attachment(categoryHierarchyId, embedd, oosBucket, timestamp);

                            attachmentsRepository.save(attachment);
                            LOGGER.info("Inserted attachment " + embedd + " under " + row.getCategoryUnstyled());
                        }
                    }
                }
                categorySeqNbr += Constants.SEQUENCE_GAP;
            }
        }

        return categorySeqNbr;
    }

}

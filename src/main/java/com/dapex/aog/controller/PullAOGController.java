package com.dapex.aog.controller;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.*;
import com.dapex.aog.exception.*;
import com.dapex.aog.factory.RestResponseHeaderFactory;
import com.dapex.aog.jpa.domain.Attachment;
import com.dapex.aog.parser.Export;
import com.dapex.aog.service.DBPullService;
import com.dapex.aog.utils.AOGWordDocUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class PullAOGController extends RestResponseHeaderFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    private final DBPullService dbPullService;

    @Autowired
    PullAOGController(DBPullService dbPullService) {
        this.dbPullService = dbPullService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public List<AOGReturn> cacheInitialization() {
        LOGGER.info("com.dapex.aog.dto.aog.AOG Cache Instantiation Started");
        final List<AOGReturn> aogReturnList = this.dbPullService.getAOGIds()
                .stream()
                .map(this.dbPullService::instantiateAOG)
                .collect(Collectors.toList());

        aogReturnList.stream().forEach(System.out::print);
        LOGGER.info("com.dapex.aog.dto.aog.AOG Cache Instantiation Completed");
        return aogReturnList;
    }

    /**
     * handler for requests to the index page
     *
     * @return successful response entity with welcome message
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<?> getHome() {
        return renderResponseEntity("Welcome to the com.dapex.aog.dto.aog.AOG");
    }

    /**
     * <p>A GET method that returns a list of AOGs</p>
     *
     * @return if successful, the method will return {@link HttpStatus#OK} ,
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/getAOGList", method = RequestMethod.GET)
    public ResponseEntity<?> getAOGList() {
        LOGGER.info("[PullAOGController] - Getting com.dapex.aog.dto.aog.AOG List");
        return Optional.of(this.dbPullService.getAOGList())
                .filter(list -> !list.isEmpty())
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(EmptyAOGListException::new);
    }

    /**
     * <p>A GET method that returns the requested com.dapex.aog.dto.aog.AOG</p>
     *
     * @param id The id of an com.dapex.aog.dto.aog.AOG to return
     * @return if successful, the method will return {@link HttpStatus#OK} ,
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/getAOG", method = RequestMethod.GET)
    public ResponseEntity<?> getAOG(@Param("id") Long id) {
        LOGGER.info(String.format("[PullAOGController] - Getting com.dapex.aog.dto.aog.AOG with id = %d", id));
        return Optional.ofNullable(this.dbPullService.getFullAOG(id))
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(EmptyAOGException::new);
    }

    /**
     * <p>A POST method that creates/adds com.dapex.aog.dto.aog.AOG to in memory cache</p>
     *
     * @param id The id of the com.dapex.aog.dto.aog.AOG to instantiate
     * @return if successful, the method will return {@link HttpStatus#ACCEPTED} ,
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} when cache failed to initialize
     */
    @Deprecated
    @RequestMapping(value = "/api/instantiate", method = RequestMethod.GET)
    public ResponseEntity<?> instantiateAOG(@RequestParam("id") Long id) {
        LOGGER.info(String.format("[PullAOGController] - Instantiating Cache for com.dapex.aog.dto.aog.AOG with id = %d", id));
        return Optional.ofNullable(this.dbPullService.instantiateAOG(id))
                .map(RestResponseHeaderFactory::renderResponseCreatedEntity)
                .orElseThrow(CacheInitializationFailureException::new);
    }

    /**
     * <p>A POST method, Returns the results of the search on an com.dapex.aog.dto.aog.AOG. It only returns the chapters
     * that match the search term/phrase</p>
     *
     * @param payload A JSON object that contains the search term, id and relevant chapter information
     * @return Returns a list of chapters corresponding to the search criteria with {@link HttpStatus#OK}
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/searchFull", method = RequestMethod.POST)
    public ResponseEntity<?> searchAOG(@RequestBody SearchInput payload) {
        LOGGER.info(String.format("[PullAOGController] - Searching Entire com.dapex.aog.dto.aog.AOG with these arguments, searchTerm = %s, aogId = %d", payload.getSearchTerm(), payload.getPlatform()));
        final AOGReturn base = dbPullService.getFullAOG(payload.getPlatform());
        return Optional.ofNullable(dbPullService.searchAOG(payload.getSearchTerm(), base))
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(EmptyAOGException::new);
    }

    /**
     * <p>A POST method, Searches based off of a list of chapters sent in through the post request</p>
     *
     * @param payload A SearchChapterInput object that contains a list of chapters to search,
     *                along with a search term and com.dapex.aog.dto.aog.AOG id.
     * @return Returns the the chapters searched for if they match the criteria with {@link HttpStatus#OK}
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/searchChapter", method = RequestMethod.POST)
    public ResponseEntity<?> searchChapterAOG(@RequestBody SearchChapterInput payload) {
        LOGGER.info(String.format("[PullAOGController] - Searching Specific chapters of com.dapex.aog.dto.aog.AOG searchTerm = %s, aogId=%d, chapters=%s ",
                payload.getSearchTerm(), payload.getPlatform(), payload.getChapters().toString()));

        final AOGReturn base = this.dbPullService.getFullAOG(payload.getPlatform());
        final AOGReturn baseReturn = this.dbPullService.getSearchChapters(payload.getChapters(), base);
        return Optional.ofNullable(this.dbPullService.searchAOG(payload.getSearchTerm(), baseReturn))
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(EmptyAOGException::new);
    }
    @RequestMapping(value = "/api/export", method = RequestMethod.GET)
    public FileSystemResource export(@RequestParam Integer platformId,
                                     @RequestParam Integer chapter,
                                     @RequestParam String fileName,
                                     HttpServletResponse response) throws IOException {
        LOGGER.info(String.format("[PullAOGController] - Exporting com.dapex.aog.dto.aog.AOG Document "));

        String aopType = AOGWordDocUtility.getAOP(platformId);
        AOGReturn base = dbPullService.getFullAOG(platformId.longValue());
        File file;
        //-1 gives us the whole com.dapex.aog.dto.aog.AOG
        if (chapter.equals(-1)) {
            Export ex = new Export();
            file = ex.writeTable(base, aopType);
        } else {
            AOGReturn baseReturn = dbPullService.getSearchChapter(chapter.longValue(), base);
            Export ex = new Export();
            file = ex.writeTable(baseReturn, aopType);
        }
        File fileTempo = getFileTempo(file);
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".doc");
        return new FileSystemResource(fileTempo);
    }

    /**
     * End point to change from Contains not subchapters to Contains subchapters
     *
     * @param changeContainer object with information for a container to be changed
     * @return Updated com.dapex.aog.dto.aog.AOG with {@link HttpStatus#OK}
     *      * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR}
     */
    @RequestMapping(value = "/api/changeSubChapterType", method = RequestMethod.POST)
    public ResponseEntity<?> changeSubChapterType(@RequestBody ContainerRemove changeContainer) {
        LOGGER.info(String.format("[PullAOGController] - Changing Container with id=%d from agoId=%d",
                changeContainer.getContainerId(), changeContainer.getAogId()));

        AOGReturn base = this.dbPullService.getFullAOG(changeContainer.getAogId());
        Long containerId = changeContainer.getContainerId();

        ResponseEntity response = Optional.of(this.dbPullService.updateToDatabase(containerId, base, changeContainer.getAogId()))
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(TablePopulateFailureException::new);

        if (response.getStatusCode().is2xxSuccessful()) {

            response = Optional.of(this.dbPullService.instantiateAOG(changeContainer.getAogId(), base, containerId))
                    .map(RestResponseHeaderFactory::renderResponseEntity)
                    .orElseThrow(UpdateFailureException::new);

            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format("[PullAOGController] - Change Container as Subchapter Type with id=%s to Cache", containerId));
            } else {
                LOGGER.warn(String.format("[PullAOGController] - Failed to Change Container as Subchapter Type with id=%s to Cache", containerId));
            }
        } else {
            LOGGER.warn(String.format("[PullAOGController] - Failed to Change Container under parent id=%s to Database",
                    changeContainer.getContainerId()));
        }
        return response;
    }

    @RequestMapping(value = "/api/changeNoSubChapterType", method = RequestMethod.POST)
    public ResponseEntity<?> changeNoSubChapterType(@RequestBody ContainerRemove changeContainer) {

        LOGGER.info(String.format("[PullAOGController] - Changing Container with id=%d from agoId=%d",
                changeContainer.getContainerId(), changeContainer.getAogId()));

        AOGReturn base = this.dbPullService.getFullAOG(changeContainer.getAogId());
        Long containerId = changeContainer.getContainerId();

        ResponseEntity response = Optional.of(this.dbPullService.concatCatagories(containerId))
                .map(RestResponseHeaderFactory::renderResponseCreatedEntity)
                .orElseThrow(TablePopulateFailureException::new);
        if (response.getStatusCode().is2xxSuccessful()) {
            response = Optional.of(this.dbPullService.instantiateAOG(changeContainer.getAogId(), base, containerId))
                    .map(RestResponseHeaderFactory::renderResponseEntity)
                    .orElseThrow(UpdateFailureException::new);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format("[PullAOGController] - Change Container as No Subchapter with id=%s to Cache", containerId));
            } else {
                LOGGER.warn(String.format("[PullAOGController] - Failed to Change Container as Subchapter with id=%s to Cache", containerId));
            }
        } else {
            LOGGER.warn(String.format("[PullAOGController] - Failed to Change Container with id=%d from Database",
                    changeContainer.getContainerId()));
        }
        return response;
    }

    @RequestMapping(value = "/api/exportCategories", method = RequestMethod.POST)
    public FileSystemResource exportCategories(@RequestBody CategoryDTO payload,
                                               HttpServletResponse response) throws IOException {
        LOGGER.info(String.format("[PullAOGController] - Exporting com.dapex.aog.dto.aog.AOG Categories Document "));

        AOGReturn base = this.dbPullService.getFullAOG(payload.getPlatformId().longValue());

        List<ReturnCategory> categoriesList = this.dbPullService.getCatagoriesByIds(base,
                payload.getChapter().longValue(),payload.getCategoryList());

        String aopType = AOGWordDocUtility.getAOP(payload.getPlatformId());

        if (categoriesList != null) {
            File file;
            Export ex = new Export();
            file = ex.writeCategories(categoriesList, aopType);
            File fileTempo = getFileTempo(file);
            response.setContentType("application/msword");
            response.setHeader("Content-Disposition", "attachment; filename=" + payload.getFileName() + ".doc");
            return new FileSystemResource(fileTempo);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/api/exportSubChapters", method = RequestMethod.POST)
    public FileSystemResource exportSubChapters(@RequestBody SubChapterDTO payload,
                                               HttpServletResponse response) throws IOException {
        LOGGER.info(String.format("[PullAOGController] - Exporting com.dapex.aog.dto.aog.AOG SubChapters Document "));

        AOGReturn base = this.dbPullService.getFullAOG(payload.getPlatformId().longValue());

        List<ReturnSubChapter> subChapterList = this.dbPullService.getSubChaptersByIds(base,
                payload.getSubChapterList());

        String aopType = AOGWordDocUtility.getAOP(payload.getPlatformId());

        if (subChapterList != null) {
            File file;
            Export ex = new Export();
            file = ex.writeSubChapters(subChapterList, aopType);
            File fileTempo = getFileTempo(file);
            response.setContentType("application/msword");
            response.setHeader("Content-Disposition", "attachment; filename=" + payload.getFileName() + ".doc");
            return new FileSystemResource(fileTempo);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/api/exportChapters", method = RequestMethod.POST)
    public FileSystemResource exportChapters(@RequestBody ChapterDTO payload,
                                                HttpServletResponse response) throws IOException {
        LOGGER.info(String.format("[PullAOGController] - Exporting com.dapex.aog.dto.aog.AOG Chapters Document "));

        AOGReturn base = this.dbPullService.getFullAOG(payload.getPlatformId().longValue());

        List<ReturnChapter> chapterList = this.dbPullService.getChaptersByIds(base,
                payload.getChapterList());

        String aopType = AOGWordDocUtility.getAOP(payload.getPlatformId());

        if (chapterList != null) {
            File file;
            Export ex = new Export();
            file = ex.writeChapters(chapterList, aopType);
            File fileTempo = getFileTempo(file);
            response.setContentType("application/msword");
            response.setHeader("Content-Disposition", "attachment; filename=" + payload.getFileName() + ".doc");
            return new FileSystemResource(fileTempo);
        } else {
            return null;
        }
    }

    /**
     * <p>Endpoint to get a list of all chapters in an com.dapex.aog.dto.aog.AOG</p>
     *
     * @param id the id of the com.dapex.aog.dto.aog.AOG that contains the chapters
     * @return Returns a list of chapters with the id and chapter name with {@link HttpStatus#OK}
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/chapterList", method = RequestMethod.GET)
    public ResponseEntity<?> getChapters(@Param("id") Long id) {
        LOGGER.info(String.format("[PullAOGController] - Getting list of chapters for aogId=%d", id));
        return Optional.ofNullable(this.dbPullService.getChapterList(id))
                .filter(list -> !list.isEmpty())
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(EmptyListException::new);
    }

    /**
     * End point to remove a Chapter/SubChapter/Category from the com.dapex.aog.dto.aog.AOG
     *
     * @param deleteContainer object with information for a container to be removed
     * @return Updated com.dapex.aog.dto.aog.AOG with {@link HttpStatus#OK}
     *      * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if deletion failed on either cache or DB
     */
    @RequestMapping(value = "/api/deleteContainer", method = RequestMethod.POST)
    public ResponseEntity<?> deleteContainer(@RequestBody ContainerRemove deleteContainer) {
        LOGGER.info(String.format("[PullAOGController] - Deleting Container with id=%d from agoId=%d",
                deleteContainer.getContainerId(), deleteContainer.getAogId()));

        AOGReturn base = this.dbPullService.getFullAOG(deleteContainer.getAogId());
        Long containerId = deleteContainer.getContainerId();

        // validation
        boolean isinDB = this.dbPullService.containerExistsDB(containerId);
        boolean isinCache = this.dbPullService.containerExistsCache(containerId, base);
        if (!isinDB && !isinCache) {
            LOGGER.info(String.format("[PullAOGController] - Container with id=%d already deleted",
                    deleteContainer.getContainerId()));
            return RestResponseHeaderFactory.renderResponseEntity(base);
        }

        ResponseEntity response = Optional.of(this.dbPullService.deleteFromDatabase(deleteContainer.getContainerId()))
                .map(RestResponseHeaderFactory::renderResponseCreatedEntity)
                .orElseThrow(TablePopulateFailureException::new);
        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info(String.format("[PullAOGController] - Deleted Container with id=%d from Database",
                    deleteContainer.getContainerId()));

            response = Optional.of(this.dbPullService.deleteFromCache(deleteContainer.getAogId(), base,
                    deleteContainer.getContainerId()))
                    .map(RestResponseHeaderFactory::renderResponseEntity)
                    .orElseThrow(UpdateFailureException::new);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format("[PullAOGController] - Deleted Container with id=%d from Cache",
                        deleteContainer.getContainerId()));
            } else {
                LOGGER.info(String.format("[PullAOGController] - Failed to Delete Container with id=%d from Cache",
                        deleteContainer.getContainerId()));
            }
        } else {
            LOGGER.warn(String.format("[PullAOGController] - Failed to Delete Container with id=%d from Database",
                    deleteContainer.getContainerId()));
        }
        return response;
    }

    /**
     * End point to add a Chapter/SubChapter/Category to the com.dapex.aog.dto.aog.AOG
     *
     * @param createContainer object with information to add a new container
     * @return updated com.dapex.aog.dto.aog.AOG with {@link HttpStatus#OK}
     *      * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if creation failed on either cache or DB
     */
    @RequestMapping(value = "/api/createContainer", method = RequestMethod.POST)
    public ResponseEntity<?> createContainer(@RequestBody ContainerCreate createContainer) {
        LOGGER.info(String.format("[PullAOGController] - Creating %s under parent id=%d in agoId=%d",
                createContainer.getContainerLabel(), createContainer.getParentId(), createContainer.getAogId()));

        AOGReturn base = this.dbPullService.getFullAOG(createContainer.getAogId());
        ResponseEntity response = Optional.of(this.dbPullService.addToDatabase(createContainer, base))
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(TablePopulateFailureException::new);
        if (response.getStatusCode().is2xxSuccessful()) {
            String savedId = response.getBody().toString();
            LOGGER.info(String.format("[PullAOGController] - Added Container under parent id=%s to Database with id=%s",
                    createContainer.getParentId(), savedId));

            response =  Optional.of(this.dbPullService.addToCache(createContainer.getAogId(), base, createContainer))
                    .map(RestResponseHeaderFactory::renderResponseEntity)
                    .orElseThrow(UpdateFailureException::new);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format("[PullAOGController] - Added Container with id=%s to Cache", savedId));
            } else {
                LOGGER.warn(String.format("[PullAOGController] - Failed to Add Container with id=%s to Cache", savedId));
            }
        } else {
            LOGGER.warn(String.format("[PullAOGController] - Failed to Add Container under parent id=%s to Database",
                    createContainer.getParentId()));
        }
        return response;
    }

    /**
     * <p>Endpoint to update a chapter</p>
     *
     * @param chapterEdit the chapter that needs to be updated
     * @return Updated com.dapex.aog.dto.aog.AOG with {@link HttpStatus#OK}
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if update failed on either cache or DB
     */
    @RequestMapping(value = "/api/updateChapter", method = RequestMethod.POST)
    public ResponseEntity<?> updateChapter(@RequestBody ChapterEdit chapterEdit) {
        ReturnChapter chapter = chapterEdit.getChapter();
        LOGGER.info(String.format(
                "[PullAOGController] - Updating chapterName=%s with chapterId=%d from aogId=%d",
                chapter.getChapterName(), chapter.getChapterId(), chapterEdit.getAogId()));

        final AOGReturn base = this.dbPullService.getFullAOG(chapterEdit.getAogId());

        ResponseEntity response = Optional.of(this.dbPullService.updateDB(chapterEdit.getAogId(), chapter, base))
                .map(RestResponseHeaderFactory::renderResponseCreatedEntity)
                .orElseThrow(TablePopulateFailureException::new);
        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info(String.format("[PullAOGController] - Chapter %s with id=%d Updated in Database",
                    chapter.getChapterName(), chapter.getChapterId()));

            response = Optional.of(this.dbPullService.updateCache(chapterEdit.getAogId(), base, chapter))
                    .map(RestResponseHeaderFactory::renderResponseEntity)
                    .orElseThrow(UpdateFailureException::new);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format("[PullAOGController] - Chapter %s with id=%d Updated in Cache",
                        chapter.getChapterName(), chapter.getChapterId()));
            } else {
                LOGGER.warn(String.format("[PullAOGController] - Failed to Update Chapter %s with id=%d in Cache",
                        chapter.getChapterName(), chapter.getChapterId()));
            }
        } else {
            LOGGER.warn(String.format("[PullAOGController] - Failed to Update Chapter %s with id=%d in Database",
                    chapter.getChapterName(), chapter.getChapterId()));
        }
        return response;
    }



    @RequestMapping(value = "/api/addAttachment", method = RequestMethod.POST)
    public ResponseEntity<?> addAttachment(@RequestBody AttachmentCreate attachmentCreate) {
        LOGGER.info(String.format(
                "[PullAOGController] - Adding new attachment fileName=%s with id=%d to aogId=%d in bucketName=%s",
                attachmentCreate.getFileName(), attachmentCreate.getContainerId(), attachmentCreate.getAogId(),
                attachmentCreate.getBucketName()));

        final AOGReturn base = this.dbPullService.getFullAOG(attachmentCreate.getAogId());
        ResponseEntity response = Optional.of(this.dbPullService.addAttachmentToDb(attachmentCreate))
                    .map(RestResponseHeaderFactory::renderResponseEntity)
                    .orElseThrow(TablePopulateFailureException::new);
        if (response.getStatusCode().is2xxSuccessful()) {
            Attachment attachment = (Attachment) response.getBody();
            LOGGER.info(String.format("[PullAOGController] - Added Attachment to containerId=%d in Database with id=%s",
                    attachmentCreate.getContainerId(), attachment.getId()));

            response = Optional.of(this.dbPullService.addAttachmentToCache(attachmentCreate.getAogId(), base, attachment))
                        .map(RestResponseHeaderFactory::renderResponseEntity)
                        .orElseThrow(UpdateFailureException::new);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format(
                        "[PullAOGController] - Added Attachment to containerId=%d in Cache with id=%d",
                        attachmentCreate.getContainerId(), attachment.getId()));
            } else {
                LOGGER.warn(String.format("[PullAOGController] - Failed to Add Attachment to containerId=%d in Cache",
                        attachmentCreate.getContainerId()));
            }
        } else {
            LOGGER.warn(String.format("[PullAOGController] - Failed to Add Attachment to containerId=%d in Database",
                    attachmentCreate.getContainerId()));
        }
        return response;
    }

    @RequestMapping(value = "/api/deleteAttachment", method = RequestMethod.POST)
    public ResponseEntity<?> deleteAttachment(@RequestBody AttachmentRemove attachmentRemove) {
        LOGGER.info(String.format(
                "[PullAOGController] - Deleting attachment from containerId=%d with id=%d  from agoId=%d",
                attachmentRemove.getContainerId(), attachmentRemove.getAttachmentId(), attachmentRemove.getAogId()));

        final AOGReturn base = this.dbPullService.getFullAOG(attachmentRemove.getAogId());
        ResponseEntity response = Optional.of(this.dbPullService.deleteAttachmentFromDB(attachmentRemove))
                    .map(RestResponseHeaderFactory :: renderResponseCreatedEntity)
                    .orElseThrow(TablePopulateFailureException::new);
        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info(String.format(
                    "[PullAOGController] - Deleted Attachment with id=%d from containerId=%d in Database",
                    attachmentRemove.getAttachmentId(), attachmentRemove.getContainerId()));

            response = Optional.of(this.dbPullService.deleteAttachmentFromCache(
                    attachmentRemove.getAogId(), base, attachmentRemove))
                        .map(RestResponseHeaderFactory::renderResponseEntity)
                        .orElseThrow(UpdateFailureException::new);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info(String.format(
                        "[PullAOGController] - Deleted Attachment with id=%d from containerId=%d in Cache",
                        attachmentRemove.getAttachmentId(), attachmentRemove.getContainerId()));
            } else {
                LOGGER.warn(String.format(
                        "[PullAOGController] - Failed to Delete Attachment with id=%d from containerId=%d in Cache",
                        attachmentRemove.getAttachmentId(), attachmentRemove.getContainerId()));
            }
        } else {
            LOGGER.warn(String.format(
                    "[PullAOGController] - Failed to Delete Attachment with id=%d from containerId=%d in Database",
                    attachmentRemove.getAttachmentId(), attachmentRemove.getContainerId()));
        }

        return response;
    }

    static File getFileTempo(File file) throws IOException {
        File fileTempo = File.createTempFile("temp", null);//new File("temporal.doc");
        BufferedReader inputStream = null;
        PrintWriter outputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(file));
            outputStream = new PrintWriter(new FileWriter(fileTempo));
            String l;
            while ((l = inputStream.readLine()) != null) {
                l = l.replace("(R)", "&#174;");
                outputStream.println(l);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return fileTempo;
    }
}

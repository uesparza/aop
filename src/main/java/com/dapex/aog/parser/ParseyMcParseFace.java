package com.dapex.aog.parser;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.*;
import com.dapex.aog.exception.ParsingFailureException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@ComponentScan
public class ParseyMcParseFace {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * This method is used with @getSections method. It parses through element by element
     * to get the location of where BAR review in TOC is found. This is the assumption of TOC starting
     * with BAR review information.
     *
     * @return the number element where benefit and administrative review is found in TOC.
     */
    private int getBarNum(XWPFDocument xdoc) {

        try {
            int start = 0;
            for (XWPFParagraph p : xdoc.getParagraphs()) {
                if (!p.getRuns().isEmpty()) {
                    if (p.getText().toLowerCase().contains(Constants.START_OF_TOC)) {
                        return start + 1;
                    }
                }
                start = start + 1;
            }
        } catch (Exception ex) {
            LOGGER.error("Something went wrong: " + ex);
            ex.printStackTrace();
        }
        LOGGER.warn("Unable to locate section: " + Constants.START_OF_TOC);
        return -1;
    }

    /**
     * Helper function that returns the trimmed version of the Paragraphs text
     *
     * @param xdoc The main document used to obtain paragraphs.
     * @param x    Gets the PARAGRAPH at element @x.
     * @return The trimmed version of the text at this PARAGRAPH location.
     */
    private String getParaText(XWPFDocument xdoc, int x) {
        return xdoc.getParagraphs().get(x).getText().trim();
    }

    /**
     * Gets the PARAGRAPH style. This is mainly used to determine chapter vs sub-chapter.
     * All Chapters in TOC will be "TOC1" and all sub-chapters will be "TOC2" style.
     *
     * @param xdoc the object we use to get the paragraphs from
     * @param x    Gets the PARAGRAPH at element @x.
     * @return Returns the style of the PARAGRAPH at given location.
     */
    private String getParaStyle(XWPFDocument xdoc, int x) {

        return xdoc.getParagraphs().get(x).getStyle();
    }

    /**
     * Starts at the location of function @getBarNum as this is where our TOC starts.
     * We then go through each element parsing its Styling. If the style is TOC1 our element is
     * a Chapter. If the style is TOC2, our element is a SubChapter. To determine if our current
     * element is a Chapter or SubChapter we look at the current element and the next element. If this
     * element and the next is TOC1, we know we are at a Chapter. If this ones TOC1 and next is TOC2,
     * we know we are at a Chapter with SubChapters. If we are at TOC2, we know this is a SubChapter still.
     *
     * @return Our TableOfContents object that lets us know what is a Chapter vs SubChapter.
     */
    private TableOfContents getSections(XWPFDocument xdoc) throws IOException {
        TableOfContents toc = new TableOfContents();
        String parentOfSubChapter = "";
        int start = getBarNum(xdoc);
        try {
            while (!getParaText(xdoc, start).isEmpty()) {
                switch (getParaStyle(xdoc, start)) {
                    case Constants.CHAPTER_STYLE:
                        if (!getParaText(xdoc, start + 1).isEmpty()) {
                            //If this element is TOC1 and next one is TOC2, then this element is a PARENT of sub-chapter
                            if (getParaStyle(xdoc, start + 1).equals(Constants.SUBCHAPTER_STYLE)) {
                                parentOfSubChapter = getParaText(xdoc, start).split("\\t")[0];
                            }
                            toc.addChapter(getParaText(xdoc, start).split("\\t")[0], null);
                        }
                        break;
                    case Constants.SUBCHAPTER_STYLE:
                        toc.addChapter(parentOfSubChapter, getParaText(xdoc, start).split("\\t")[0]);
                        break;
                    default:
                        LOGGER.error("Unable to parse style: " + getParaStyle(xdoc, start));
                        break;
                }
                start = start + 1;
            }

        } catch (Exception ex) {
            LOGGER.error("Unexpected Error: " + ex);
            ex.printStackTrace();
        } finally {
            xdoc.close();
        }
        return toc;
    }

    /**
     * Determine the file extension based on parsing the XML.
     * The only strange one is "Package" which we give bin. A bin file can be of type
     * zip and PDF from what I have seen.
     *
     * @param unknown The unknown XML type we are determining extension for
     * @return The file extension we are about to upload to OOS.
     */
    private String determineType(String unknown) {
        switch (unknown) {
            case "Word.Document.12":
                return ".docx";
            case "Word.Document.8":
                return ".doc";
            case "Acrobat":
                return ".bin";
            case "Excel.Sheet.12":
                return ".xlsx";
            case "Excel.Sheet.8":
                return ".xls";
            case "Package":
                return ".bin";
            case "PowerPoint.Show.8":
                return ".ppt";
            case "PowerPoint.Show.12":
                return ".pptx";
            default:
                return null;
        }
    }

    /**
     * Gets the value of a Row at a given location and removes all un-necessary spaces.
     *
     * @param row The object we are parsing for the given text
     * @param x   The location of which we are parsing
     * @return The String representation of the value at location @x in the @row.
     */
    private String getValueOfRowPos(XWPFTableRow row, int x) {

        try {
            return new Styling().getBullets(row.getCell(x));
            //return row.getCell(x).getText();
        } catch (NullPointerException e) {
            return Constants.LEFT_BLANK;
        }
    }

    /**
     * This gets all cells/text within a category of a Table only. It looks to get all Embedded documents in this
     * section and then gets the name of the document. We then use our HashMap to look up the file and see the name it was
     * uploaded as to OOS so we can correctly tie it to this category. The name of the file comes from the ProgID field of the XML
     * We then parse this to verify what type of document we are viewing.
     *
     * @param row    This is the row it parses to verify if there is an embedded doc.
     * @param sorted This is the SORTED list of embedded documents still left. It contains everything, we then have to sort further based on type
     * @param lookup Lookup allows us to tie a file name uploaded to S3 to the embedded document name in POI
     * @return The list of embedded documents found in this particular @CategoryRow object
     */
    private List<String> getEmbedds(XWPFTableRow row, List<PackagePart> sorted, HashMap<String, String> lookup, String oosBucketName) {

        List<String> embedds = new ArrayList<>();
        row.getTableCells().forEach((cell) -> {
            cell.getParagraphs().forEach((paragraph) -> {
                paragraph.getCTP().getRList().forEach((embed) -> {
                    if (!embed.getObjectList().isEmpty()) {
                        String type = determineType(
                                embed.getObjectList()
                                        .get(0)
                                        .xmlText()
                                        .split("ProgID=\"")[1]
                                        .split("\"")[0]
                        );
                        if (type != null) {
                            List<PackagePart> results = sorted.stream().filter(sort ->
                                    sort.getPartName()
                                            .getName()
                                            .endsWith(type))
                                    .collect(Collectors.toList());
                            if (getExtension(results.get(0).getContentType()).equals(".ole")) {
                                try {
                                    String ext = parseOle(results.get(0), 0, false, oosBucketName);
                                    embedds.add("https://" + oosBucketName + ".s3api-core.uhc.com/" +
                                            Constants.FILE_UPLOAD_PREFIX +
                                            lookup.get(results.get(0).getPartName().getName()) + ext);
                                } catch (IOException | InterruptedException | Ole10NativeException e) {
                                    LOGGER.error("Something bad happened..." + e.getMessage());
                                }
                            } else {
                                embedds.add("https://" + oosBucketName + ".s3api-core.uhc.com/" +
                                        Constants.FILE_UPLOAD_PREFIX +
                                        lookup.get(results.get(0).getPartName().getName()) +
                                        getExtension(results.get(0).getContentType()));
                            }
                            sorted.remove(results.get(0));
                        }
                    }
                });
            });
        });
        return embedds;
    }


    private String getUnstyledRowValue(XWPFTableRow row, int x) {

        try {
            return row.getCell(x).getText().trim();
        } catch (NullPointerException e) {
            return Constants.LEFT_BLANK;
        }
    }


    /**
     * Gets all definitions in a given table. A definition is defined as a cell with 2 columns.
     * The first is the key and the second is the value of that key.
     *
     * @param table The object we are parsing to find definitions
     * @return A map of definitions found in <K,V> format.
     */
    private Map<String, String> getDefinitions(XWPFTable table) {

        Map<String, String> deffs = new HashMap<>();
        table.getRows().forEach((row) -> {
            //2 columns are defined as definitions
            if (row.getTableCells().size() == 2 &&
                    !row.getCell(0).getText().trim().isEmpty() &&
                    !row.getCell(1).getText().trim().isEmpty()) {
                deffs.put(
                        new Styling().getBullets(row.getCell(0)),
                        new Styling().getBullets(row.getCell(1))
                );
            }
        });
        return deffs;
    }


    /**
     * This populates a row Object IFF standard format with 4 columns (Category, Standard,
     * Non-Standard and Bar Required.
     *
     * @param table This is what is parsed to get standard 4 column setup.
     * @return Our list of Rows found in this standard format. If none are found, just return none for all values.
     */
    private List<CategoryRow> populateStandardTableIntoRow(XWPFTable table,
                                                           List<PackagePart> sorted,
                                                           HashMap<String, String> lookup, String oosBucketName) {

        List<CategoryRow> rows = new ArrayList<>();
        table.getRows().forEach((row) -> {
            if (row.getTableCells().size() == 4) {
                //we dont want to include headers
                if (!row.getCell(0).getText().equals(Constants.CATEGORY_NAME)) {
                    //Check to make sure the category doesn't already exist in table
                    rows.add(
                            new CategoryRow(getValueOfRowPos(row, 0),
                                    getUnstyledRowValue(row, 0),
                                    getValueOfRowPos(row, 1),
                                    getValueOfRowPos(row, 2),
                                    getValueOfRowPos(row, 3),
                                    getEmbedds(row, sorted, lookup, oosBucketName)
                            )
                    );
                }
            }
        });
        if (rows.size() == 0) {
            //if there are ONLY definitions, you will get back an object of all none
            rows.add(new CategoryRow(
                    Constants.BLANKS,
                    Constants.BLANKS,
                    Constants.BLANKS,
                    Constants.BLANKS,
                    Constants.BLANKS,
                    null));
        }
        return rows;
    }

    /**
     * This gets our Table Overview. A table overview is defined as one massive block of text
     * with no columns. If one is found, it adds it to the list. This function does NOT handle
     * text over tables which is also defined as an overview.
     *
     * @param table This is our table object we are parsing to see if it contains overviews.
     * @return Our list of overviews found in our @table object.
     */
    private List<String> getTableOverview(XWPFTable table) {

        List<String> overview = new ArrayList<>();
        table.getRows().forEach((row) -> {
            if (row.getTableCells().size() == 1 && !row.getCell(0).getText().trim().isEmpty()) {
                overview.add(new Styling().getBullets(row.getCell(0)));
            }
        });
        return overview;
    }


    /**
     * This function is used to populate the List of Chapters returned back to the API. This handles logic
     * of whether we are dealing with a Chapter with no sub-chapters, or a Chapter that has sub-chapters.
     * It then appropriatley creates a Chapter object on this.
     *
     * @param toc      This is the Table Of Contents used to see if we are dealing with a Chapter or Sub-Chapter.
     * @param chapters This is our current List of Chapters so we can append onto it.
     * @param table    This is the current table we are looking at so we can parse through it.
     * @param title    This is the title that was found from the run function.
     * @param overview This is text found outside of the table or in one massive block inside the table.
     */
    private void addTableToChapter(TableOfContents toc, List<Chapter> chapters,
                                   XWPFTable table, String title, List<String> overview,
                                   List<PackagePart> sorted, HashMap<String, String> lookup, String oosBucketName) {

        List<Table> tables = new ArrayList<>();
        List<CategoryRow> rows = new ArrayList<>();
        Table t = new Table(rows);
        tables.add(t);
        rows.addAll(populateStandardTableIntoRow(table, sorted, lookup, oosBucketName));
        if (!title.isEmpty()) {
            //This is a Sub-chapter
            if (toc.getChapters().containsKey(title)) {
                //TITLE: Main chapter name  - with the FIRST sub-chapter
                if (toc.getChapters().get(title).size() > 1) {
                    //Unable to get Title
                    if (overview.size() == 0) {
                        chapters.add(new Chapter(title, new ArrayList<>(Collections.singletonList(
                                new SubChapter(
                                        "ERROR: Unable to get Title",
                                        getDefinitions(table), tables))),
                                "rballas",
                                "today"));
                    } else {
                        chapters.add(new Chapter(title, new ArrayList<>(Collections.singletonList(
                                new SubChapter(
                                        overview.get(0),
                                        getDefinitions(table), tables))),
                                "rballas",
                                "today"));
                    }
                } else {
                    //Create a Chapter that has no sub-chapter
                    List<String> overviews = new ArrayList<>(overview);
                    overviews.addAll(getTableOverview(table));
                    chapters.add(new Chapter(
                            title,
                            overviews,
                            getDefinitions(table),
                            "rballas",
                            "today",
                            tables)
                    );
                }
            } else {
                //This is a SubChapter that already has a chapter and at least one sub-chapter
                chapters.get(chapters.size() - 1).addSubChapter(new SubChapter(title, getDefinitions(table), tables));
            }
        }
    }

    /**
     * This function is used to append to a previous Overview. This looks for any row in the table that has no columns.
     * If this is found, it is defined as an overview, and appended to our overview list.
     *
     * @param chap This is our chapter object. We get our over view, then append to it
     * @param tab  This is used to see if any overviews are found in the form of one massive block (no columns)
     */
    private void appendOverview(Chapter chap, XWPFTable tab) {
        List<String> currentOverview = chap.getOverviews();
        List<String> additionalOverview = getTableOverview(tab);
        if (additionalOverview != null) {
            if (currentOverview != null) {
                currentOverview.addAll(additionalOverview);
            } else {
                chap.setOverviews(additionalOverview);
            }
        }
    }


    /**
     * This takes a table with no title. This means logically it must belong to the previous section.
     * So we take the last @chap and see if it has sub chapters. if it does, we add the table to
     * the last sub chapter found. If the chapter does NOT have sub chapters, we can add the table directly
     * to our Chapter object.
     *
     * @param chap This is our chapter object used to determine if we have sub chapters. If we do, we append to sub-chapter.
     * @param tab  This is the table we are appending onto either our Chapter or SubChapter
     */
    private void appendTable(Chapter chap, Table tab) {
        if (chap.getSubChapters() != null) {
            //Get the last sub chapter and add a table to it
            chap.getSubChapters().get(chap.getSubChapters().size() - 1).getTables().add(tab);
        } else {
            //other wise add a table to main chapter
            chap.getTables().add(tab);
        }
    }

    /**
     * This takes a table with no title. This means logically it must belong to the previous section.
     * So we take the last @chap and see if it has sub chapters. if it does, we add the table to
     * the last sub chapter found. If the chapter does NOT have sub chapters, we can add the table directly
     * to our Chapter object.
     *
     * @param chap       This is our chapter object used to determine if we have sub chapters. If we do, we append to sub-chapter.
     * @param definition This is the definition we want to append.
     */
    private void appendDefinition(Chapter chap, Map<String, String> definition) {
        if (chap.getSubChapters() != null) {
            //Get the last sub chapter and add a table to it
            chap.getSubChapters().get(chap.getSubChapters().size() - 1).getDefinitions().putAll(definition);
        } else {
            //other wise add a table to main chapter
            chap.getDefinitions().putAll(definition);
        }
    }

    private String getExtension(String name) {

        switch (name) {
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.template":
                return ".dotx";
            case "application/vnd.ms-word.document.macroEnabled.12":
                return ".docm";
            case "application/vnd.ms-word.template.macroEnabled.12":
                return ".dotm";
            case "application/vnd.ms-excel":
                return ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ".xlsx";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.template":
                return ".xltx";
            case "application/vnd.ms-excel.sheet.macroEnabled.12":
                return ".xlsm";
            case "application/vnd.ms-excel.template.macroEnabled.12":
                return ".xltm";
            case "application/vnd.ms-excel.addin.macroEnabled.12":
                return ".xlam";
            case "application/vnd.ms-excel.sheet.binary.macroEnabled.12":
                return ".xlsb";
            case "application/vnd.ms-powerpoint":
                return ".ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.template":
                return ".potx";
            case "application/vnd.openxmlformats-officedocument.presentationml.slideshow":
                return ".ppsx";
            case "application/vnd.ms-powerpoint.addin.macroEnabled.12":
                return ".ppam";
            case "application/vnd.ms-powerpoint.presentation.macroEnabled.12":
                return ".pptm";
            case "application/vnd.ms-powerpoint.template.macroEnabled.12":
                return ".potm";
            case "application/vnd.ms-powerpoint.slideshow.macroEnabled.12":
                return ".ppsm";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return ".pptx";
            case "application/vnd.ms-access":
                return ".mdb";
            case "application/vnd.openxmlformats-officedocument.oleObject":
                return ".ole";
            default:
                LOGGER.error("Unknown MIME detected: " + name);
                return ".unknown";
        }
    }

    /**
     * Hexdump the file to determine if its a PDF or not
     *
     * @param content The byte content we are analyzing
     * @return if its a PDF or not
     */
    private String getExtByByte(byte[] content) {

        if (Arrays.toString(content).startsWith("[37, 80, 68, 70, 45")) {
            return ".pdf";
        }
        return ".unknown";
    }

    /**
     * This function takes in a DocumentInputStream and creates the file to be uploaded to OOS.
     * If its a zip file we have to do a built in Unix command called Fix which helps recover the
     * file if its de-compressed incorrectly. At the end we upload the File to OOS.
     *
     * @param fs     The file system being used
     * @param fn     The name of the InputStream file that gets written to the file
     * @param i      Name of the file uploaded to S3 appended to "Attatchment"
     * @param ext    The file extension we are going to use when uploading.
     * @param upload This will determine if we actually do the upload, or just get the file extension
     * @return The file extension of the file that was just uploaded to S3
     */
    private String writeStream(POIFSFileSystem fs, String fn, int i, String ext, boolean upload, String oosBucketName)
            throws IOException, InterruptedException {

        try (DocumentInputStream is = fs.createDocumentInputStream(fn)) {
            byte[] content = new byte[is.available()];
            is.read(content);
            if (ext == null) {
                uploadFile(content, Constants.FILE_UPLOAD_PREFIX + i + getExtByByte(content), oosBucketName);
                return getExtByByte(content);
            } else {
                if (ext.equals(".zip")) {
                    File targetFile = java.io.File.createTempFile(Constants.FILE_UPLOAD_PREFIX + i, ".tmp");
                    try (OutputStream outStream = new FileOutputStream(targetFile)) {
                        outStream.write(content);
                    }
                    Runtime r = Runtime.getRuntime();
                    File outputFix = java.io.File.createTempFile("zip_fix", ".tmp");
                   // Process p = r.exec("zip -FFv " + targetFile.getPath() + " --out " + outputFix.getPath());
                    Process p = r.exec(new String[]{"zip -FFv ", targetFile.getPath() , " --out " , outputFix.getPath()});

                    p.waitFor();
                    if (upload) {
                        uploadFile(Files.readAllBytes(outputFix.toPath()), Constants.FILE_UPLOAD_PREFIX + i + ext, oosBucketName);
                    }
                    return ext;
                } else {
                    if (upload) uploadFile(content, Constants.FILE_UPLOAD_PREFIX + i + ext, oosBucketName);
                    return ext;
                }
            }
        }
    }

    /**
     * Parses the FileSystem created by Office. There are two different options when you extract.
     * Theres is an Ole10Native and a CONTENTS section. Contents is generated if its a zip file
     * with only one file and Ole10Native is for a zip with multiple files.
     *
     * @param pPart  The PackagePart (Embedded Document) that we are currently parsing.
     * @param i      This will be the name of the package in Optum Object Store
     * @param upload This allows us to get the extension of a @pPart without actually uploading
     * @return A string representing the extension that was just uploaded to OOS
     */
    private String parseOle(PackagePart pPart, int i, boolean upload, String oosBucketName)
            throws IOException, InterruptedException, Ole10NativeException {

        try (InputStream is = pPart.getInputStream();
             POIFSFileSystem fs = new POIFSFileSystem(is)) {
            DirectoryNode root = fs.getRoot();
            if (root.hasEntry("\u0001Ole10Native")) {
                Ole10Native ole = Ole10Native.createFromEmbeddedOleObject(fs);
                String ext = ole.getFileName().substring(ole.getFileName().lastIndexOf(".")).toLowerCase();
                String fn = root.getEntry("\u0001Ole10Native").getName();
                return writeStream(fs, fn, i, ext, upload, oosBucketName);
            } else if (root.hasEntry("CONTENTS")) {
                String fn = root.getEntry("CONTENTS").getName();
                return writeStream(fs, fn, i, null, upload, oosBucketName);
            } else {
                LOGGER.error("Section not parsed");
                return ".unknown";
            }
        }
    }


    /**
     * This does the actual uploading of the byte buffer to OOS.
     * It has authorization headers so it is allowed to upload.
     *
     * @param buffer   the buffer that is written to S3
     * @param fileName the name of the file it will have when uploaded
     */
    private void uploadFile(byte[] buffer, String fileName, String oosBucketName) {
        HttpURLConnection httpConn = null;
        try {
            String uri = "https://s3api-core.uhc.com/" + oosBucketName + "/" + fileName;
            URL url = new URL(uri);
            String accessKey = System.getenv("ACCESS_KEY");
            String secretKey = System.getenv("SECRET_KEY");
            String method = "PUT";
            // object-name is like file-name.  Object names can include slashes to simulate directory structure.
            httpConn = (HttpURLConnection) url.openConnection();
            String resource = "/" + oosBucketName + "/" + fileName;
            String contentType = "multipart/form-data";
            SimpleDateFormat df = new SimpleDateFormat(
                    "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
            Date date = new Date();
            String formattedDate = df.format(date);


            String signn = method + "\n\n" + contentType + "\n" + formattedDate
                    + "\n" + resource;


            Mac hmac = null;
            try {
                hmac = Mac.getInstance("HmacSHA1");
                hmac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                LOGGER.error("Secret keys failed: " + e.getMessage());
            }

            String signature = new String(Base64.encodeBase64(hmac.doFinal(signn
                    .getBytes(StandardCharsets.UTF_8)))).replaceAll("\n", Constants.LEFT_BLANK);


            String authAWS = "AWS " + accessKey + ":" + signature;


            httpConn.setRequestMethod(method);
            httpConn.setDoOutput(true);
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Date", formattedDate);
            httpConn.setRequestProperty("Content-type", contentType);
            httpConn.setRequestProperty("Authorization", authAWS);
            try (OutputStream os = httpConn.getOutputStream();
                 BufferedOutputStream bos = new BufferedOutputStream(os)) {
                for (byte s : buffer) {
                    bos.write(s);
                }
            } catch (IOException e) {
                LOGGER.error("Write to output stream failed: " + e.getMessage());
            }
            httpConn.getInputStream();
        } catch (Exception e) {
            LOGGER.error("Error in upload: " + e.getMessage());
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }


    /**
     * This does the uploading of the files to S3 however it does not upload them in the correct order.
     * We must create a hashmap to link files back to the integer they were uploaded as. This is used
     * to perserve order in the document. This is needed because @getAllEmbeds does NOT perserve order
     * of documents by default.
     *
     * @return A HashMap with the names of the files Uploaded
     */
    private HashMap<String, String> parseDocuments(XWPFDocument xdoc, String oosBucketName) {

        HashMap<String, String> docLookup = new HashMap<>();
        try {
            List<PackagePart> initialStream = xdoc.getAllEmbedds();
            int i = 0;
            for (PackagePart pPart : initialStream) {
                if (getExtension(pPart.getContentType()).equals(".ole")) {
                    parseOle(pPart, i, true, oosBucketName);
                    docLookup.put(pPart.getPartName().getName(), Integer.toString(i));
                } else {
                    try (InputStream is = pPart.getInputStream()) {
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);
                        docLookup.put(pPart.getPartName().getName(), Integer.toString(i));
                        LOGGER.info("Uploading attachment_" + i + getExtension(pPart.getContentType()));
                        uploadFile(buffer,
                                Constants.FILE_UPLOAD_PREFIX + i + getExtension(pPart.getContentType()),
                                oosBucketName);
                    }
                }
                i++;
            }
        } catch (IOException | InterruptedException | Ole10NativeException | OpenXML4JException e) {
            LOGGER.error("Error in Doc parsing: " + e.getMessage());
        }
        return docLookup;
    }

    /**
     * It first starts by getting our Table of Contents object to see what is a chapter and what is a sub-chapter.
     * Then it parses through element by element seeing if we are at a Paragraph or Table element. The way we parse
     * this is we assume the first text encountered is our title. There then is a piece of optional text above the
     * table which is our overview. Then we hit a table element where we start parsing it. It works closely with the
     * the above function @addTableToChapter to determine chapter vs sub-chapter.
     *
     * @return This returns our list of Chapter Objects used by the API
     */
    public List<Chapter> run(String aogDocument, String oosBucketName) throws ParsingFailureException {

        List<Chapter> chapters = new ArrayList<>();
        List<String> overview = new ArrayList<>();
        boolean start = false;
        ZipSecureFile.setMinInflateRatio(0);
        try (InputStream fis = this.getClass().getResourceAsStream("/" + aogDocument);
             XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis))) {

            TableOfContents toc = getSections(xdoc);
            HashMap<String, String> lookup = parseDocuments(xdoc, oosBucketName);
            final List<PackagePart> sorted = xdoc.getAllEmbedds().stream().sorted().collect(Collectors.toList());

            Iterator<IBodyElement> bodyElementIterator = xdoc.getBodyElementsIterator();
            String title = "";
            while (bodyElementIterator.hasNext()) {
                IBodyElement element = bodyElementIterator.next();
                switch (element.getElementType().toString()) {
                    case Constants.ELEMENT_TABLE:
                        String tableContent = ((XWPFTable) element).getText().trim().toLowerCase();
                        if (!tableContent.equals(Constants.TOC)) {
                            if (!title.isEmpty() || !(chapters.size() > 0)) {
                                //Benefits that may or may not require a Non-Standard Admin BAR request. Please review the AOG prior to BAR submission
                                addTableToChapter(
                                        toc,
                                        chapters,
                                        ((XWPFTable) element),
                                        title,
                                        overview,
                                        sorted,
                                        lookup,
                                        oosBucketName);
                                title = Constants.LEFT_BLANK;
                                overview.clear();
                            } else {
                                //We found a second table with no title, so it must belong to the previous section
                                Chapter cp = chapters.get(chapters.size() - 1);
                                appendTable(cp, new Table(populateStandardTableIntoRow(
                                        (XWPFTable) element,
                                        sorted,
                                        lookup,
                                        oosBucketName
                                )));
                                appendDefinition(cp, getDefinitions((XWPFTable) element));
                                appendOverview(cp, ((XWPFTable) element));
                            }
                        }
                        break;

                    case Constants.ELEMENT_PARAGRAPH:
                        String text = ((XWPFParagraph) element).getText();
                        if (!text.trim().isEmpty() && !text.trim().toLowerCase(Locale.ENGLISH).equals(Constants.TOC)) {
                            if (title.isEmpty()) {
                                //Used so we can skip the first section of the Doc
                                if (((XWPFParagraph) element).getText().trim()
                                        .equals(toc.getChapters()
                                                .entrySet()
                                                .iterator()
                                                .next()
                                                .getKey()) || start) {
                                    start = true;
                                    title = ((XWPFParagraph) element).getText().trim();
                                }
                            } else {
                                overview.add(text.trim());
                            }
                        }
                        break;

                    default:
                        LOGGER.error("Unknown element type: " + element.getElementType().toString());
                }
            }
            return chapters;
        } catch (IOException | OpenXML4JException e) {
            e.printStackTrace();
            throw new ParsingFailureException();
        }
    }
}

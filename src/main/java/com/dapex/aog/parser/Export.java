package com.dapex.aog.parser;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.AOGReturn;
import com.dapex.aog.dto.ReturnCategory;
import com.dapex.aog.dto.ReturnChapter;
import com.dapex.aog.dto.ReturnSubChapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Export {

    private Document parseIt(String html) {
        return Jsoup.parse(html);
    }

    private Document removeNulls(Document doc) {
        doc.getAllElements().forEach((element) -> {
            if (element.ownText().equals("null")) {
                element.remove();
            }
        });
        return doc;
    }

    private Document tableWidth(Document doc) {
        doc.getAllElements().forEach((element) -> {
            if (element.tagName().equals("a")) {
                element.wrap("<p style='word-break: break-all'> </p>");
            }
        });
        return doc;
    }

    private Document alignTop(Document doc) {
        doc.getAllElements().forEach((element) -> {
            element.attributes().forEach((attr) -> {
                String style = attr.getValue();
                if (style.contains("border: 1px solid #dddddd;")) {
                    style = style.replace("border: 1px solid #dddddd;", "border: 1px solid #dddddd; vertical-align: top;");
                    attr.setValue(style);
                }
            });
        });
        return doc;
    }

    private String addAOGName(String aogName) {
        String style = "<style type=\"text/css\">\n"+
                "       @page { \n"+
                "           size: landscape;\n"+
                "       }\n"+
                "       a {\n" +
                "           color:blue;\n" +
                "           text-decoration:underline;\n" +
                "       }\n" +
                "   </style>";
        return style + "<meta charset=\"UTF-8\" /><span style= \"font-family: arial; text-align:center\"><strong>AOP: </strong>" + aogName + "</span><br /><br />";
        //ISO-8859-1
    }

    private String addChapterName(String name) {
        name = cleanString(name);
        return "<span style= \"font-family: arial; text-align:center\">" +
                "<strong>Chapter Name: </strong><span>" + name + "</span></span><p></p><br />";
    }

    private String addSubChapterName(String name) {
        name = cleanString(name);
        return "<span style= \"font-family: arial\"><strong>Sub-Chapter Name: " +
                "</strong><span>" + name + "</span></span><p></p><br />";
    }

    private String cleanString(String text) {
        text = text.replaceAll("–", "-");
        text = text.replaceAll("’", "'");
        text = text.replaceAll("“", "\"");
        text = text.replaceAll("”", "\"");
        text = text.replaceAll("‘", "'");
        text = text.replaceAll("•", "-");
        text = text.replaceAll("…", "...");
        text = text.replaceAll("½", "1/2");
        text = text.replaceAll("™", "TM");
        text = text.replaceAll("©", "(C)");
        text = text.replaceAll("&ndash;", "-");
        text = text.replaceAll("&rsquo;", "'");
        text = text.replaceAll("&lsquo;", "'");
        text = text.replaceAll("&ldquo;", "\"");
        text = text.replaceAll("&rdquo;", "\"");
        text = text.replaceAll("&reg;", "(R)");
        text = text.replaceAll("&bull; &nbsp; &nbsp;", "- ");
        text = text.replaceAll("&trade;", "- ");
        text = text.replaceAll("&le;", "<=");
        text = text.replaceAll("&bull;&nbsp; &nbsp;&nbsp;", "- ");
        text = text.replace("<span>Ctrl + Click to open in a new tab</span>", "");
        text = text.replace("Ctrl + Click to open in a new tab", "");
        text = text.replace(" .", ".");
        text = text.replace("contenteditable=\"false\"","");
        text = text.replaceAll("<li><h3>", "<li><p>");
        text = text.replaceAll("</h3></li>", "</p></li>");
        text = text.replaceAll("\t", "");
        text = text.replace("<table style=\"width: 100%;\">","<table border=\"1\" style=\"width: 100%;\">");
        return text;
    }

    private String addOverview(String text) {
        text = cleanString(text);
        return "<p style= \"font-family: arial\">" + text + "</p><br />";
    }

    private String addCategory(String catname, String standard, String nonstandard, String bar, String exceptions, String additional, String allAttachments) {
        if (standard == null) standard = "";
        if (nonstandard == null) nonstandard = "";
        if (bar == null) bar = "";
        if (exceptions == null) exceptions = "";
        if (additional == null) additional = "";
        catname = cleanString(catname);
        standard = cleanString(standard);
        nonstandard = cleanString(nonstandard);
        bar = cleanString(bar);
        exceptions = cleanString(exceptions);
        additional = cleanString(additional);
        final String bgGreen = "#43a047";
        final String bgRed =  "#ef5350";
        final String bgYellow = "#ff9800";
        final String bgGrey = "#6c757d";
        final String bgCanvasColor = "#FFFFFF";
        return "<tr><th bgcolor=\"#4E7DC2\" style=\"font-family: arial; width:20%; border: 1px solid #dddddd;\"> CATEGORY </th>" +
                "<th bgcolor=\""+bgGreen+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\"> STANDARD </th>" +
                "<th bgcolor=\""+bgRed+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\"> EXCEPTION REQUIRED </th>" +
                "<tr><td bgcolor=\""+bgCanvasColor+"\" rowspan=\"8\" style=\"font-family: arial; width:20%; border: 1px solid #dddddd;\">" + catname + "</td>" +
                "<td bgcolor=\""+bgCanvasColor+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\">" + standard + "</td>" +
                "<td bgcolor=\""+bgCanvasColor+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\">" + bar + "</td>" +
                "<tr><th bgcolor=\""+bgYellow+"\"style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\"> NOT PREFERRED BUT ACCEPTABLE" + "</th>" +
                "<th bgcolor=\""+bgRed+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\"> NO EXCEPTIONS </th></tr>" +
                "<tr><td bgcolor=\""+bgCanvasColor+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\">" + nonstandard + "</td>" +
                "<td bgcolor=\""+bgCanvasColor+"\" style=\"font-family: arial; width:40%; border: 1px solid #dddddd;\">" + exceptions + "</td></tr>" +
                "<tr><th bgcolor=\""+bgGrey+"\" colspan=\"2\" style=\"font-family: arial; border: 1px solid #dddddd;\"> ADDITIONAL INFORMATION </th></tr>" +
                "<tr><td bgcolor=\""+bgCanvasColor+"\" colspan=\"2\" style=\"font-family: arial; border: 1px solid #dddddd;\">" + additional + "</td><tr>" +
                "<tr><th bgcolor=\"#D9DED8\" colspan=\"2\" style=\"font-family: arial; border: 1px solid #dddddd;\"> ATTACHMENTS </th></tr>" +
                "<tr><td bgcolor=\""+bgCanvasColor+"\" colspan=\"2\" style=\"font-family: arial; border: 1px solid #dddddd;\">" + allAttachments + "</td></tr>" +
                "<tr><td bgcolor=\"#E4ECF9\" align=\"center\" colspan=\"3\"> Internal Use Only / Confidential/Proprietary/Competitively Sensitive Information</td></tr>";
    }

    private String addDefintion(String def) {
        def = cleanString(def);
        return "<span style= \"font-family: arial\"><p>" + def.replace("||", Constants.LEFT_BLANK) + "</p></span><br />";
    }

    private String addAttachments(String attach) {
        return "<span style= \"font-family: arial\"><p>" + attach + "</p></span>";

    }

    public File writeTable(AOGReturn aog, String aogName) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(addAOGName(aogName));
        aog.getResults().forEach((result) -> {
            final String[] chapAttachments = {""};
            html.append(addChapterName(result.getChapterName()));
            if (!result.getOverviews().isEmpty()) {
                html.append(Constants.OVERVIEW_HTML_TITLE);
                result.getOverviews().forEach((overview) -> html.append(addOverview(overview.getText())));
            }
            if (!result.getDefinitions().isEmpty()) {
                html.append(Constants.DEFINTION_HTML_TITLE);
                result.getDefinitions().forEach((definition) -> html.append(addDefintion(definition.getText())));
            }
            result.getAttachments().forEach(attachment -> {
                chapAttachments[0] = attachment.getFilename() + "<br />" + chapAttachments[0];
                attachment.getAhref();
            });
            html.append(Constants.ATTACHMENTS_HTML_TITLE);
            html.append(addAttachments(chapAttachments[0]));
            if (result.getSubChapterList().isEmpty()) {
                if (result.getCategories() != null) {
                    html.append(Constants.TABLE_START);
                    result.getCategories().forEach((cat) -> {
                        final String[] allAttachments = {""};
                        if (!cat.getAttachments().isEmpty()) {
                            cat.getAttachments().forEach(attachment -> {
                                allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                            });
                        }
                        html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                    });
                    html.append(Constants.CLOSE_TABLE_HTML);
                }
            } else {
                //SUB-CHAPTER

                result.getSubChapterList().forEach((subchap) -> {
                    html.append("<br><br/>");
                    html.append(addSubChapterName(subchap.getSubChapterName()));
                    html.append(Constants.TABLE_START);
                    if (subchap.getCategories() != null) {
                        html.append(Constants.TABLE_START);
                        subchap.getCategories().forEach((cat) -> {
                            final String[] allAttachments = {""};
                            if (!cat.getAttachments().isEmpty()) {
                                cat.getAttachments().forEach(attachment -> {
                                    allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                                });
                            }
                            html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                    cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                                });

                        html.append(Constants.CLOSE_TABLE_HTML);
                    }
                });
            }
        });
        File targetFile = java.io.File.createTempFile("export", ".docx");
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(alignTop(tableWidth(removeNulls(parseIt(html.toString())))).toString().getBytes());
        }
        return targetFile;
    }

    public File writeChapters(List<ReturnChapter> chapterList, String aogName) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(addAOGName(aogName));

        chapterList.forEach((result) -> {
            final String[] chapAttachments = {""};
            html.append(addChapterName(result.getChapterName()));
            if (!result.getOverviews().isEmpty()) {
                html.append(Constants.OVERVIEW_HTML_TITLE);
                result.getOverviews().forEach((overview) -> html.append(addOverview(overview.getText())));
            }
            if (!result.getDefinitions().isEmpty()) {
                html.append(Constants.DEFINTION_HTML_TITLE);
                result.getDefinitions().forEach((definition) -> html.append(addDefintion(definition.getText())));
            }
            result.getAttachments().forEach(attachment -> {
                chapAttachments[0] = attachment.getFilename() + "<br/>" + chapAttachments[0];
            });
            html.append(Constants.ATTACHMENTS_HTML_TITLE);
            html.append(addAttachments(chapAttachments[0]));
            if (result.getSubChapterList().isEmpty()) {
                if (result.getCategories() != null) {
                    html.append(Constants.TABLE_START);
                    result.getCategories().forEach((cat) -> {
                        final String[] allAttachments = {""};
                        if (!cat.getAttachments().isEmpty()) {
                            cat.getAttachments().forEach(attachment -> {
                                allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                            });
                        }
                        html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                    });
                    html.append(Constants.CLOSE_TABLE_HTML);
                }
            } else {
                //SUB-CHAPTER

                result.getSubChapterList().forEach((subchap) -> {
                    html.append("<br><br/>");
                    html.append(addSubChapterName(subchap.getSubChapterName()));
                    html.append(Constants.TABLE_START);
                    if (subchap.getCategories() != null) {
                        html.append(Constants.TABLE_START);
                        subchap.getCategories().forEach((cat) -> {
                            final String[] allAttachments = {""};
                            if (!cat.getAttachments().isEmpty()) {
                                cat.getAttachments().forEach(attachment -> {
                                    allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                                });
                            }
                            html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                    cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                        });

                        html.append(Constants.CLOSE_TABLE_HTML);
                    }
                });
            }
        });
        File targetFile = java.io.File.createTempFile("export", ".docx");
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(alignTop(tableWidth(removeNulls(parseIt(html.toString())))).toString().getBytes());
        }
        return targetFile;
    }


    public File writeSubChapters(List<ReturnSubChapter> subChapterList, String aogName) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(addAOGName(aogName));
                        //SUB-CHAPTER

                subChapterList.forEach((subchap) -> {
                    html.append("<br><br/>");
                    html.append(addSubChapterName(subchap.getSubChapterName()));
                    html.append(Constants.TABLE_START);
                    if (subchap.getCategories() != null) {
                        html.append(Constants.TABLE_START);
                        subchap.getCategories().forEach((cat) -> {
                            final String[] allAttachments = {""};
                            if (!cat.getAttachments().isEmpty()) {
                                cat.getAttachments().forEach(attachment -> {
                                    allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                                });
                            }
                            html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                    cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                        });

                        html.append(Constants.CLOSE_TABLE_HTML);
                    }
                });

        File targetFile = java.io.File.createTempFile("export", ".docx");
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(alignTop(tableWidth(removeNulls(parseIt(html.toString())))).toString()
                    .getBytes());
        }
        return targetFile;
    }


    public File writeCategories(List<ReturnCategory> categoryList, String aogName) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(addAOGName(aogName));

        categoryList.forEach((category) -> {
            if (!"".equals(category.getSubChapter()))
                html.append(category.getChapterName() +" => "+category.getSubChapter()+" => "+category.getCategoryName());
            else
                html.append(category.getChapterName() +" => "+category.getCategoryName());
            html.append(Constants.TABLE_START);

            final String[] allAttachments = {""};
            if (!category.getAttachments().isEmpty()) {
                category.getAttachments().forEach(attachment -> {
                    allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                });
            }
            html.append(addCategory(category.getCategoryName(), category.getStandard(), category.getNonstandard(),
                    category.getBar(), category.getNoExceptions(), category.getAdditionalInformation(), allAttachments[0]));
            html.append(Constants.CLOSE_TABLE_HTML);

        });
        File targetFile = java.io.File.createTempFile("export", ".docx");
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            System.out.println(html.toString());
            outStream.write(alignTop(tableWidth(removeNulls(parseIt(html.toString())))).toString()
                    .getBytes());
            outStream.flush();
        }
        return targetFile;
    }

    public File writeCategories(AOGReturn aog, String aogName) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(addAOGName(aogName));
        aog.getResults().forEach((result) -> {
            html.append(addChapterName(result.getChapterName()));
            result.getAttachments().forEach(attachment -> {
            });
            if (result.getSubChapterList().isEmpty()) {
                if (result.getCategories() != null) {
                    html.append(Constants.TABLE_START);
                    result.getCategories().forEach((cat) -> {
                        final String[] allAttachments = {""};
                        if (!cat.getAttachments().isEmpty()) {
                            cat.getAttachments().forEach(attachment -> {
                                allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                            });
                        }
                        html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                    });
                    html.append(Constants.CLOSE_TABLE_HTML);
                }
            } else {
                //SUB-CHAPTER

                result.getSubChapterList().forEach((subchap) -> {
                    html.append(addSubChapterName(subchap.getSubChapterName()));
                    html.append(Constants.TABLE_START);
                    if (subchap.getCategories() != null) {
                        html.append(Constants.TABLE_START);
                        subchap.getCategories().forEach((cat) -> {
                            final String[] allAttachments = {""};
                            if (!cat.getAttachments().isEmpty()) {
                                cat.getAttachments().forEach(attachment -> {
                                    allAttachments[0] = attachment.getFilename() + "<br />" + allAttachments[0];
                                });
                            }
                            html.append(addCategory(cat.getCategoryName(), cat.getStandard(), cat.getNonstandard(),
                                    cat.getBar(), cat.getNoExceptions(), cat.getAdditionalInformation(), allAttachments[0]));
                        });

                        html.append(Constants.CLOSE_TABLE_HTML);
                    }
                });
            }
        });
        File targetFile = java.io.File.createTempFile("export", ".docx");
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(alignTop(tableWidth(removeNulls(parseIt(html.toString())))).toString().getBytes());
        }
        return targetFile;
    }

}


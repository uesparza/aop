package com.dapex.aog.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Element {

    private String color;

    private int size;

    private String text;

    private boolean isBold;

    private boolean inLine;

    private boolean isItalic;

    private boolean isUnderline;

    private String linkAnchor;

    private List<Element> runs;

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isInLine() {
        return inLine;
    }

    public void setInLine(boolean inLine) {
        this.inLine = inLine;
    }

    public List<Element> getRuns() {
        return runs;
    }

    private void setRuns(List<Element> runs) {
        this.runs = runs;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setItalic(boolean italic) {
        isItalic = italic;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean underline) {
        this.isUnderline = underline;
    }

    public String getLink() {
        return linkAnchor;
    }

    public void setLinkAnchor(String linkAnchor) {
        this.linkAnchor = linkAnchor;
    }

    public void appendRun(Element run) {
        if (getRuns() == null) {
            setRuns(new ArrayList<>(Collections.singletonList(run)));
        } else {
            runs.add(run);
        }
    }

    /**
     * @return HTML version of this element given the above attributes.
     */
    public String toStringParagraph() {
        StringBuilder styling = new StringBuilder();
        // add styling
        styling.append("<p style='font-family: arial; color: #");
        styling.append(runs.get(0).getColor()).append(";'>");
        for (Element run: runs) {
            String content = run.getText();
            // format content
            if (run.isBold()) {
                content = "<strong>" + content + "</strong>";
            }
            if (run.isItalic()) {
                content = "<em>" + content + "</em>";
            }
            if (run.isUnderline()) {
                content = "<u>" + content + "</u>";
            }

            // add hyperlink
            String link = run.getLink();
            if (run.getLink() != null) {
                styling.append("<a ");
                // detect email
                if (run.getText().contains("@")) {
                    // ensure email links prepended with 'mailto:'
                    link = link.replaceAll("(?:mailto:)?(?:.*[/])?(.+)$", "mailto:$1");
                } else {
                    styling.append("target='_blank' ");
                }
                styling.append("title='").append(link).append("&#10;Ctrl + Click to open link in a new tab' ");
                styling.append("href='").append(link).append("'>");
                styling.append(content).append("</a>");
            } else {
                styling.append(content);
            }
        }
        styling.append("</p>");
        return styling.toString();
    }

}

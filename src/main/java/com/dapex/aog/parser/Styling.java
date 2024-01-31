package com.dapex.aog.parser;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.BulletedItem;
import com.dapex.aog.dto.Element;
import com.dapex.aog.dto.ElementsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public class Styling {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * This method iterates through each instance in ElementMap. If its an Element object,
     * then we know its a paragraph and add it with a P tag. If its not an element object,
     * its a bulletedItem object. This means we need to check to see if it has children or not,
     * then add the parent / children accordingly.
     *
     * @param em Our elements map object that we iterate through to generate the HTML
     * @return a String builder HTML thats returned to API
     */
    private String renderHTML(ElementsMap em) {
        StringBuilder sb = new StringBuilder();
        List<Element> bi = em.getBullets();
        bi.forEach((element) -> {
            //Paragraph
            if (element.getClass().isInstance(new Element())) {
                sb.append(element.toStringParagraph());
            } else {
                //Bullet that has no children
                BulletedItem bullets = (BulletedItem) element;
                if (bullets.getChildren() == null) {
                    sb.append(((BulletedItem) element).toStringParent((BulletedItem) element)).append("</ul>");
                } else {
                    //Bullet w/ children
                    sb.append(((BulletedItem) element).toStringChild((BulletedItem) element)).append("</ul>");
                }
            }
        });
        String html = sb.toString();
        // combine solo unordered lists
        return html.replaceAll("<[/]ul>((?=<ul).)ul>", "");
    }

    /**
     * We first check to see if Bold is set at the run level or the PARENT level. If its set at the CHILD level, isSetB() will return
     * true and then we can just get the CHILD's value. If its not set at CHILD level, this means we inherited from the PARENT. We then
     * try to get the Bold from the PARENT. If its still not set here, it must be set from higher up (which we are assuming is inherited
     * from Default style. Default style is not bold so we just return false.
     *
     * @param run The run object we are parsing
     * @return A boolean representing if this run is italicized or not.
     */
    private boolean isBold(XWPFRun run) {
        if (run.getCTR().isSetRPr()) {
            if (run.getCTR().getRPr().isSetB()) {
                return run.isBold();
            } else {
                try {
                    return run.getDocument()
                            .getStyles()
                            .getStyle(((XWPFParagraph) run.getParent()).getStyle())
                            .getCTStyle()
                            .getRPr()
                            .isSetB();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * We first check to see if Italics is set at the run level or the PARENT level. If its set at the CHILD level, isSetI() will return
     * true and then we can just get the CHILD's value. If its not set at CHILD level, this means we inherited from the PARENT. We then
     * try to get the Italics from the PARENT. If its still not set here, it must be set from higher up (which we are assuming is inherited
     * from Default style. Default style is not italics so we just return false.
     *
     * @param run The run object we are parsing
     * @return A boolean representing if this run is italicized or not.
     */
    private boolean isItalicized(XWPFRun run) {
        if (run.getCTR().isSetRPr()) {
            if (run.getCTR().getRPr().isSetI()) {
                return run.isItalic();
            } else {
                try {
                    return run.getDocument()
                            .getStyles()
                            .getStyle(((XWPFParagraph) run.getParent()).getStyle())
                            .getCTStyle()
                            .getRPr()
                            .isSetI();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * We first check to see if Underline is set at the run level or the PARENT level. If its set at the CHILD level, isSetU() will return
     * true and then we can just get the CHILD's value. If its not set at CHILD level, this means we inherited from the PARENT. We then
     * try to get the Underline from the PARENT. If its still not set here, it must be set from higher up (which we are assuming is inherited
     * from Default style. Default style is not underline so we just return false.
     *
     * @param run The run object we are parsing
     * @return A boolean representing if this run is Underline or not.
     */
    private boolean isUnderline(XWPFRun run) {
        if (run.getCTR().isSetRPr()) {
            if (run.getCTR().getRPr().isSetU()) {
                return run.getUnderline() != UnderlinePatterns.NONE;
            } else {
                try {
                    return run.getDocument()
                            .getStyles()
                            .getStyle(((XWPFParagraph) run.getParent()).getStyle())
                            .getCTStyle()
                            .getRPr()
                            .isSetU();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the run contains a Hyperlink. If it does then it returns the hyperlink reference string.
     *
     * @param run The run object we are parsing
     * @return A string representing the hyperlink reference or null if none exists.
     */
    private String getLinked(XWPFRun run) {
        if (run instanceof XWPFHyperlinkRun) {
            XWPFHyperlink hyperlink = ((XWPFHyperlinkRun) run).getHyperlink(run.getDocument());
            if (hyperlink != null) {
                return hyperlink.getURL();
            }
        }
        return null;
    }

    /**
     * Gets the color of a run. If its not easy to find, we default to black for the time being.
     *
     * @param run The run object we are parsing
     * @return A string representing the color. Sometimes its in hex, sometimes its a word representing color.
     */
    private String getColor(XWPFRun run) {
        if (run.getColor() == null) {
            return "000000";
        }
        return run.getColor();
    }

    /**
     * Gets the font Size of a given run. If its not easy to find, we default it to 8pt for the time being.
     *
     * @param run The run object we are parsing
     * @return an int representing the font size of this particular run.
     */
    private int getFontSize(XWPFRun run) {
        if (run.getFontSize() == -1) {
            return 9;
        }
        return run.getFontSize();
    }


    /**
     * Sets the styles defined from the helper functions above
     *
     * @param run the run object it gets styling from
     * @return an element that has the proper styling attached
     */
    private Element setStyle(XWPFRun run) {
        Element para = new Element();
        // substitutes multiple spaces or random carriage returns from runs for a single space
        String runText = run.text().replaceAll("\n|\\s{2,}", " ");
        if (!runText.isEmpty()) {
            para.setText(runText);
            para.setColor(getColor(run));
            para.setSize(getFontSize(run));
            para.setBold(isBold(run));
            para.setItalic(isItalicized(run));
            para.setUnderline(isUnderline(run));
            para.setLinkAnchor(getLinked(run));
        }
        return para;
    }

    /**
     * Take the current @para and process it as either a Parent, Child or PARAGRAPH. Create either an
     * Element or BulletedItem then append that onto our ElementsMap.
     *
     * @param para     The current PARAGRAPH to be processed
     * @param type     This is either a PARENT, CHILD or PARAGRAPH. Depending on which, do the needful
     * @param elements Our final elementsMap that is returned to the API
     * @return a new ElementsMap that has the additional styling from our PARAGRAPH.
     */
    private ElementsMap process(XWPFParagraph para, String type, ElementsMap elements) {
        switch (type) {
            case Constants.PARENT:
                BulletedItem parent = new BulletedItem();
                elements.addParentBullet((BulletedItem) handleElement(parent, para));
                break;
            case Constants.CHILD:
                BulletedItem child = new BulletedItem();
                elements.addChildBullet((BulletedItem) handleElement(child, para));
                break;
            case Constants.PARAGRAPH:
                Element paragraph = new Element();
                elements.addParagraph((Element) handleElement(paragraph, para));
                break;
            default:
                LOGGER.warn("Unknown Element detected: " + type);
        }
        return elements;
    }


    /**
     * Get the runs and see if you are dealing with a @PARAGRAPH that has 1 run or more than 1 run.
     * If its more than 1, get all the runs, and append them on. This also sets all to inLine for right now,
     * which may be changed in future versions. Otherwise, we process just that 1 run.
     *
     * @param addedElement This is either a BulletedItem or an Element object
     * @param paragraph    The current PARAGRAPH to be processed
     * @return either a BulletedItem or an Element object depending on type
     */
    private Object handleElement(Element addedElement, XWPFParagraph paragraph) {

        if (paragraph.getRuns().size() >= 2) {
            for (XWPFRun run : paragraph.getRuns()) {
                Element newRun = setStyle(run);
                if (newRun.getText() != null) {
                    newRun.setInLine(true);
                    addedElement.appendRun(newRun);
                }
            }
        } else if (paragraph.getRuns() != null && paragraph.getRuns().size() != 0) {
            Element run = setStyle(paragraph.getRuns().get(0));
            addedElement.appendRun(run);

        }
        return addedElement;
    }

    /**
     * We iterate through each PARAGRAPH of the cell. If the NumFmt is null, this means we are looking at
     * a PARAGRAPH rather than a bullet. We then get the indentation level of the PARAGRAPH. If its -1 or 720
     * we know we are looking at a Parent bullet. Otherwise we are looking at a CHILD.
     *
     * @param cell The Cell we are going to parse to see if it has paragraphs or bullets in it
     * @return This is the rendered HTML of the bullets returned to the API
     */
    public String getBullets(XWPFTableCell cell) {
        ElementsMap elements = new ElementsMap();
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            if (paragraph.getNumFmt() != null) {
                //System.out.println(PARAGRAPH.getText() + " " + PARAGRAPH.getIndentationLeft());
                if (paragraph.getIndentationLeft() != -1 && paragraph.getIndentationLeft() != 720) {
                    //Parent Bullet
                    elements = process(paragraph, Constants.PARENT, elements);
                } else {
                    //Child Bullet
                    elements = process(paragraph, Constants.CHILD, elements);
                }
            } else if (!paragraph.getText().trim().isEmpty()) {
                // Paragraph
                elements = process(paragraph, Constants.PARAGRAPH, elements);

            }
        }
        return renderHTML(elements);
    }

}

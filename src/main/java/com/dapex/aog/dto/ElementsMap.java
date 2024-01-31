package com.dapex.aog.dto;

import java.util.ArrayList;
import java.util.List;

public class ElementsMap {


    /**
     * This holds a list of both Bullets and Elements. We iterate through to se which we are looking
     * at so we render the proper HTML
     */
    private List<Element> elements;

    public ElementsMap() {
        this.elements = new ArrayList<>();
    }

    public void addParentBullet(BulletedItem newContent) {
        this.addParagraph(newContent);
    }

    /**
     * @param newContent Adds a new Paragraph object to our elements list
     */
    public void addParagraph(Element newContent) {
        this.elements.add(newContent);
    }

    /**
     * Adds a CHILD bullet to to the PARENT
     *
     * @param newContent this is the new CHILD we are adding to the PARENT
     */
    public void addChildBullet(BulletedItem newContent) {

        if (getBullets().isEmpty()) {
            //If we try to add a Child before a CHILD, add it as a Parent instead
            addParentBullet(newContent);
        } else {
            //Adding a CHILD to PARENT that exists
            Element lastKey = elements.get(elements.size() - 1);
            if (lastKey instanceof BulletedItem) {
                BulletedItem currentChildren = (BulletedItem) lastKey;
                currentChildren.appendChild(newContent);
            } else {
                addParagraph(newContent);
            }
        }
    }

    public List<Element> getBullets() {
        return elements;
    }
}

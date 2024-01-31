package com.dapex.aog.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BulletedItem extends Element {

    private List<BulletedItem> children;

    public List<BulletedItem> getChildren() {
        return children;
    }

    public void appendChild(BulletedItem child) {
        if (getChildren() == null) {
            setChildren(new ArrayList<>(Collections.singletonList(child)));
        } else {
            children.add(child);
        }
    }

    private void setChildren(List<BulletedItem> children) {
        this.children = children;
    }

    /**
     * @return HTML version of a PARENT list object
     */
    public String toStringParent(BulletedItem parent) {
        String styling = "<ul><li>";
        styling += parent.toStringParagraph();
        return styling + "</li>";
    }

    /**
     * @param element The object used to get the attributes of the CHILD
     * @return HTML version of this CHILD's attributes
     */
    public String toStringChild(BulletedItem element) {
        StringBuilder styling = new StringBuilder();
        styling.append(toStringParent(element)).append("<ul>");
        for (BulletedItem elementChildren: element.getChildren()) {
            styling.append("<li>");
            styling.append(elementChildren.toStringParagraph());
            styling.append("</li>");
        }
        return styling.append("</ul>").toString();
    }

}

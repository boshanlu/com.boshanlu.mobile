package com.boshanlu.mobile.widget.htmlview;


public class HtmlNode {
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_UNDEFINE = -1;
    public static final int DEC_UNDERLINE = 1;
    public static final int DEC_LINE_THROUGH = 2;
    public static final int DEC_NONE = 0;
    public static final int DEC_UNDEFINE = -1;
    public int type = HtmlTag.UNKNOWN;
    public String name;
    public int start = -1;
    public HtmlAttr attr;

    public HtmlNode(int type, String name, HtmlAttr attr) {
        this.type = type;
        this.name = name;
        this.attr = attr;
    }

    @Override
    public String toString() {
        return "name:" + name + ", type:" + type + ", attr:{" + attr;
    }

    public static class HtmlAttr {
        public String src;//attr
        public String href;//attr
        public int color = AttrParser.COLOR_NONE;//css,attr color
        public int fontSize = -1;// TODO: 2017/5/10
        public int textAlign = ALIGN_UNDEFINE;
        public int textDecoration = DEC_UNDEFINE;
        public int align = ALIGN_UNDEFINE;//布局方向 block元素起作用


        @Override
        public String toString() {
            return "{color:" + color
                    + (src == null ? "" : ", src:" + src)
                    + (href == null ? "" : ", href:" + href) + "}";
        }
    }
}

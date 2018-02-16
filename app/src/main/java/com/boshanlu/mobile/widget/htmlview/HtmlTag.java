package com.boshanlu.mobile.widget.htmlview;


public class HtmlTag {

    public static final int UNKNOWN = -1;

    /**
     * 内联标签
     */
    public static final int FONT = 1;//不赞同字体color face size
    public static final int TT = 2;//等宽的文本效果
    public static final int I = 3;//斜体
    public static final int CITE = 4;//引用斜体
    public static final int DFN = 5;//定义斜体
    public static final int U = 6;//下划线
    public static final int BIG = 7;
    public static final int SMALL = 8;
    public static final int EM = 9;//强调的内容斜体
    public static final int STRONG = 10;//语气更强的强调
    public static final int B = 11;//加粗
    public static final int KBD = 12;//定义键盘文本
    public static final int MARK = 13;//突出显示部分文本
    public static final int A = 14; //href
    //img 标签比较特殊 当为
    public static final int IMG = 15; //src
    public static final int BR = 16;
    public static final int SUB = 17;
    public static final int SUP = 18;
    public static final int INS = 19;//下划线
    public static final int DEL = 20;//删除线
    public static final int S = 21;//不赞同删除线
    public static final int STRIKE = 22;//不赞同删除线DEL替代
    public static final int SPAN = 23;
    public static final int Q = 24;//引用
    public static final int CODE = 25;//代码

    /**
     * 块标签
     */
    public static final int HEADER = 50;
    public static final int FOOTER = 51;
    public static final int DIV = 53;

    public static final int P = 54;
    public static final int UL = 55;
    public static final int OL = 56;
    public static final int LI = 57;

    public static final int H1 = 61;
    public static final int H2 = 62;
    public static final int H3 = 63;
    public static final int H4 = 64;
    public static final int H5 = 65;
    public static final int H6 = 66;

    public static final int PRE = 70;
    public static final int BLOCKQUOTE = 71;
    public static final int HR = 72;

    public static final int TABLE = 81;
    public static final int CAPTION = 82;
    public static final int THEAD = 83;
    public static final int TFOOT = 84;
    public static final int TBODY = 85;
    public static final int TR = 86;
    public static final int TH = 87;
    public static final int TD = 88;

    public static final int VEDIO = 91; //src
    public static final int AUDIO = 92; //src

    public static boolean isBolckTag(int i) {
        return i >= 50;
    }
}

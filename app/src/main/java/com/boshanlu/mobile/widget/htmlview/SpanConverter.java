package com.boshanlu.mobile.widget.htmlview;

import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;

import com.boshanlu.mobile.widget.htmlview.callback.ImageGetter;
import com.boshanlu.mobile.widget.htmlview.callback.ImageGetterCallBack;
import com.boshanlu.mobile.widget.htmlview.callback.SpanClickListener;
import com.boshanlu.mobile.widget.htmlview.callback.ViewChangeNotify;
import com.boshanlu.mobile.widget.htmlview.spann.Bold;
import com.boshanlu.mobile.widget.htmlview.spann.Code;
import com.boshanlu.mobile.widget.htmlview.spann.Heading;
import com.boshanlu.mobile.widget.htmlview.spann.Hr;
import com.boshanlu.mobile.widget.htmlview.spann.Image;
import com.boshanlu.mobile.widget.htmlview.spann.Italic;
import com.boshanlu.mobile.widget.htmlview.spann.Li;
import com.boshanlu.mobile.widget.htmlview.spann.Link;
import com.boshanlu.mobile.widget.htmlview.spann.Pre;
import com.boshanlu.mobile.widget.htmlview.spann.Quote;
import com.boshanlu.mobile.widget.htmlview.spann.Strike;
import com.boshanlu.mobile.widget.htmlview.spann.StyleSpan;
import com.boshanlu.mobile.widget.htmlview.spann.Sub;
import com.boshanlu.mobile.widget.htmlview.spann.Super;
import com.boshanlu.mobile.widget.htmlview.spann.UnderLine;

import java.io.IOException;
import java.util.Stack;


public class SpanConverter implements ParserCallback, ImageGetterCallBack {
    private static final String TAG = SpanConverter.class.getSimpleName();
    private String mSource;
    private SpannableStringBuilder spannedBuilder;
    private ImageGetter imageGetter;
    private SpanClickListener clickListener;
    private HtmlParser parser;
    private Stack<HtmlNode> nodes;
    private ViewChangeNotify notify;
    private int position;

    private SpanConverter(String source, ImageGetter imageGetter,
                          SpanClickListener listener, ViewChangeNotify notify) {
        mSource = source;
        this.imageGetter = imageGetter;
        this.clickListener = listener;
        parser = new HtmlParser();
        nodes = new Stack<>();
        parser.setHandler(this);
        position = 0;
        this.notify = notify;
    }

    public static Spanned convert(String source, ImageGetter imageGetter,
                                  SpanClickListener listener, ViewChangeNotify notify) {

        SpanConverter converter = new SpanConverter(source, imageGetter, listener, notify);
        return converter.startConvert();
    }

    private Spanned startConvert() {
        try {
            parser.parse(mSource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spannedBuilder;
    }

    @Override
    public void startDocument(int len) {
        spannedBuilder = new SpannableStringBuilder();
    }

    @Override
    public void startElement(HtmlNode node) {
        if (HtmlTag.isBolckTag(node.type)) {
            handleBlockTag(true, node.type, position, null);
        }
        switch (node.type) {
            case HtmlTag.UNKNOWN:
                break;
            case HtmlTag.BR:
                handleBlockTag(false, node.type, position, node.attr);
                break;
            case HtmlTag.IMG:
                handleImage(position, node.attr.src);
                break;
            case HtmlTag.HR:
                handleHr(position);
                break;
            default:
                node.start = position;
                nodes.push(node);
                break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        spannedBuilder.append(new String(ch, start, length));
        position += length;
        //还要根据栈顶的元素类型添加适当的\n
    }

    @Override
    public void endElement(int type, String name) {
        if (type == HtmlTag.UNKNOWN
                || type == HtmlTag.BR
                || type == HtmlTag.IMG
                || type == HtmlTag.HR
                || nodes.isEmpty()) {
            return;
        }

        if (nodes.peek().type != type) {
            return;
        }

        HtmlNode node = nodes.pop();
        int start = node.start;

        switch (type) {
            case HtmlTag.H1:
            case HtmlTag.H2:
            case HtmlTag.H3:
            case HtmlTag.H4:
            case HtmlTag.H5:
            case HtmlTag.H6:
                handleHeading(start, type - HtmlTag.H1 + 1);
                break;
            case HtmlTag.P:
                handleParagraph(start, node.attr);
                break;
            case HtmlTag.B:
            case HtmlTag.STRONG:
                setSpan(start, new Bold());
                break;
            case HtmlTag.A:
                handleUrl(start, node.attr.href);
                break;
            case HtmlTag.I:
            case HtmlTag.EM:
            case HtmlTag.CITE:
            case HtmlTag.DFN:
                setSpan(start, new Italic());
                break;
            case HtmlTag.DEL:
            case HtmlTag.S:
            case HtmlTag.STRIKE:
                setSpan(start, new Strike());
                break;
            case HtmlTag.U:
            case HtmlTag.INS:
                setSpan(start, new UnderLine());
                break;
            case HtmlTag.UL:
            case HtmlTag.OL:
            case HtmlTag.DIV:
            case HtmlTag.HEADER:
            case HtmlTag.FOOTER:
                break;
            case HtmlTag.LI:
                setSpan(start, new Li());
                break;
            case HtmlTag.PRE:
                setSpan(start, new Pre());
                break;
            case HtmlTag.BLOCKQUOTE:
                handleBlockquote(start);
                break;
            case HtmlTag.Q:
            case HtmlTag.CODE:
            case HtmlTag.KBD:
                setSpan(start, new Code());
                break;
            case HtmlTag.MARK:
                break;
            case HtmlTag.SPAN:
                break;
            case HtmlTag.FONT:
                handleFont(start, node.attr);
                break;
            case HtmlTag.BIG:
                break;
            case HtmlTag.SMALL:
                break;
            case HtmlTag.SUB:
                setSpan(start, new Sub());
                break;
            case HtmlTag.SUP:
                setSpan(start, new Super());
                break;
            case HtmlTag.TT:
                //Monospace
                break;
            case HtmlTag.TABLE:
            case HtmlTag.CAPTION:
            case HtmlTag.THEAD:
            case HtmlTag.TFOOT:
            case HtmlTag.TBODY:
            case HtmlTag.TH:
            case HtmlTag.TD:
                break;
        }

        if (HtmlTag.isBolckTag(type)) {
            handleBlockTag(false, type, start, node.attr);
        }
    }

    @Override
    public void endDocument() {
    }


    //div ul 等块状标签
    private void handleBlockTag(boolean isStart, int type, int start, HtmlNode.HtmlAttr attr) {
        if (position <= 0) return;
        if (spannedBuilder.charAt(position - 1) != '\n') {
            spannedBuilder.append('\n');
            position++;
        }

        //结束block 标签
        if (!isStart && attr != null) {
            Layout.Alignment align;
            if (attr.align == HtmlNode.ALIGN_LEFT) {
                align = Layout.Alignment.ALIGN_NORMAL;
            } else if (attr.align == HtmlNode.ALIGN_RIGHT) {
                align = Layout.Alignment.ALIGN_OPPOSITE;
            } else if (attr.align == HtmlNode.ALIGN_CENTER) {
                align = Layout.Alignment.ALIGN_CENTER;
            } else {
                align = null;
            }

            if (align != null) {
                setSpan(start, position, new AlignmentSpan.Standard(align));
            }
        }
    }


    //level h1-h6
    private void handleHeading(int start, int level) {
        setSpan(start, new Heading(level));
        //spannedBuilder.setSpan(new StyleSpan(Typeface.BOLD), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    //font 标签
    private void handleFont(int start, HtmlNode.HtmlAttr attr) {
        if (attr == null) return;

        StyleSpan span;
        Object[] spans = spannedBuilder.getSpans(start, position, StyleSpan.class);
        if (spans != null && spans.length > 0) {
            int pos1 = spannedBuilder.getSpanStart(spans[0]);
            int pos2 = spannedBuilder.getSpanEnd(spans[0]);
            if (pos1 == start && pos2 == position) {//一摸一样位置的同样font
                span = (StyleSpan) spans[0];
                span.updateStyle(attr);
                return;
            }
        }

        setSpan(start, new StyleSpan(attr));
    }

    //p 标签 text-align属性
    private void handleParagraph(int start, HtmlNode.HtmlAttr attr) {
        if (attr == null) return;
        setSpan(start, new StyleSpan(attr));

        Layout.Alignment align;
        if (attr.textAlign == HtmlNode.ALIGN_LEFT) {
            align = Layout.Alignment.ALIGN_NORMAL;
        } else if (attr.textAlign == HtmlNode.ALIGN_RIGHT) {
            align = Layout.Alignment.ALIGN_OPPOSITE;
        } else if (attr.textAlign == HtmlNode.ALIGN_CENTER) {
            align = Layout.Alignment.ALIGN_CENTER;
        } else {
            align = null;
        }

        if (align != null) {
            setSpan(start, position, new AlignmentSpan.Standard(align));
        }
    }

    private void handleBlockquote(int start) {
        spannedBuilder.insert(start, "“ ");
        spannedBuilder.append("”");
        position += 3;
        setSpan(start, new Quote());
    }

    private void handleUrl(int start, String url) {
        setSpan(start, new Link(url, clickListener));
    }

    private void handleImage(int start, String url) {
        spannedBuilder.append("\uFFFC");
        position++;
        if (imageGetter != null) {
            imageGetter.getDrawable(url, start, position, this);
        }
    }

    private void handleHr(int start) {
        spannedBuilder.append(' ');
        position++;
        setSpan(start, new Hr());
    }


    private void setSpan(int start, Object span) {
        setSpan(start, position, span);
    }

    private void setSpan(int start, int end, Object span) {
        if (end <= start || end > spannedBuilder.length()) return;
        spannedBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    @Override
    public void onImageReady(String source, int start, int end, Drawable d) {
        Image[] is = spannedBuilder.getSpans(start, end, Image.class);
        for (Image i : is) {
            spannedBuilder.removeSpan(i);
        }
        setSpan(start, end, new Image(source, d));
        notify.notifyViewChange();
    }
}

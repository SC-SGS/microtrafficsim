package microtrafficsim.utils.strings.builder;

/**
 * @author Dominic Parga Cacheiro
 */
public class LevelStringBuilder implements StringBuilder {

    private java.lang.StringBuilder stringBuilder;
    private int                     level          = 0;
    private String                  levelSubString = "    ";
    private String                  levelSeparator = "\n";


    public LevelStringBuilder() {
        this.stringBuilder = new java.lang.StringBuilder();
    }

    public LevelStringBuilder(int capacity) {
        this.stringBuilder = new java.lang.StringBuilder(capacity);
    }

    public LevelStringBuilder(String str) {
        this.stringBuilder = new java.lang.StringBuilder(str);
    }

    public LevelStringBuilder(CharSequence seq) {
        this.stringBuilder = new java.lang.StringBuilder(seq);
    }


    @Override
    public String toString() {
        return stringBuilder.toString();
    }


    public void incLevel() {
        level++;
    }

    public void decLevel() {
        level--;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private LevelStringBuilder appendLevelSeparator(int level) {
        for (int i = 0; i < level; i++)
            stringBuilder.append(levelSubString);
        return this;
    }

    public String getLevelSubString() {
        return levelSubString;
    }

    public void setLevelSubString(String levelSubString) {
        this.levelSubString = levelSubString;
    }

    public String getLevelSeparator() {
        return levelSeparator;
    }

    public void setLevelSeparator(String levelSeparator) {
        this.levelSeparator = levelSeparator;
    }


    @Override
    public LevelStringBuilder append(Object obj) {
        return append(obj.toString());
    }

    /**
     * Adds the level separator times the number of level this builder has for every line (occurrence of the defined
     * level separator).
     *
     * @param str
     * @return this for practical purposes
     */
    @Override
    public LevelStringBuilder append(String str) {

        String[] lines = str.split(levelSeparator);

        if (lines.length > 0) {
            // add all lines except for last line
            for (int i = 0; i < lines.length - 1; i++) {
                appendLevelSeparator(level);
                stringBuilder.append(lines[i]).append(levelSeparator);
            }
            // add last line without line break
            appendLevelSeparator(level);
            stringBuilder.append(lines[lines.length - 1]);
        }
        int lastIdx = str.lastIndexOf(levelSeparator);
        if (lastIdx >= 0) {
            // check if last line has line break and add line break if true
            if (str.lastIndexOf(levelSeparator) == str.length() - levelSeparator.length())
                stringBuilder.append(levelSeparator);
        }

        return this;
    }

    @Override
    public LevelStringBuilder append(StringBuffer sb) {
        return append(String.valueOf(sb));
    }

    @Override
    public LevelStringBuilder append(CharSequence s) {
        return append(String.valueOf(s));
    }

    @Override
    public LevelStringBuilder append(CharSequence s, int start, int end) {
        java.lang.StringBuilder tmp = new java.lang.StringBuilder();
        tmp.append(s, start, end);
        return append(tmp.toString());
    }

    @Override
    public LevelStringBuilder append(char[] str) {
        return append(String.valueOf(str));
    }

    @Override
    public LevelStringBuilder append(char[] str, int offset, int len) {
        return append(String.valueOf(str, offset, len));
    }

    @Override
    public LevelStringBuilder append(boolean b) {
        return append(String.valueOf(b));
    }

    @Override
    public LevelStringBuilder append(char c) {
        return append(String.valueOf(c));
    }

    @Override
    public LevelStringBuilder append(int i) {
        return append(String.valueOf(i));
    }

    @Override
    public LevelStringBuilder append(long lng) {
        return append(String.valueOf(lng));
    }

    @Override
    public LevelStringBuilder append(float f) {
        return append(String.valueOf(f));
    }

    @Override
    public LevelStringBuilder append(double d) {
        return append(String.valueOf(d));
    }

    @Override
    public LevelStringBuilder appendCodePoint(int codePoint) {
        java.lang.StringBuilder tmp = new java.lang.StringBuilder();
        tmp.appendCodePoint(codePoint);
        return append(tmp.toString());
    }


    @Override
    public LevelStringBuilder appendln(Object obj) {
        append(obj);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(String str) {
        append(str);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(StringBuffer sb) {
        append(sb);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(CharSequence s) {
        append(s);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(CharSequence s, int start, int end) {
        append(s, start, end);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(char[] str) {
        append(str);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(char[] str, int offset, int len) {
        append(str, offset, len);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(boolean b) {
        append(b);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(char c) {
        append(c);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(int i) {
        append(i);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(long lng) {
        append(lng);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(float f) {
        append(f);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendln(double d) {
        append(d);
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public LevelStringBuilder appendlnCodePoint(int codePoint) {
        append(codePoint);
        stringBuilder.append("\n");
        return this;
    }


    @Override
    public LevelStringBuilder delete(int start, int end) {
        stringBuilder.delete(start, end);
        return this;
    }

    @Override
    public LevelStringBuilder deleteCharAt(int index) {
        stringBuilder.deleteCharAt(index);
        return this;
    }


    @Override
    public LevelStringBuilder replace(int start, int end, String str) {
        stringBuilder.replace(start, end, str);
        return this;
    }


    @Override
    public LevelStringBuilder insert(int index, char[] str, int offset, int len) {
        stringBuilder.insert(index, str, offset, len);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, Object obj) {
        stringBuilder.insert(offset, obj);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, String str) {
        stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, char[] str) {
        stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int dstOffset, CharSequence s) {
        stringBuilder.insert(dstOffset, s);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        stringBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, boolean b) {
        stringBuilder.insert(offset, b);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, char c) {
        stringBuilder.insert(offset, c);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, int i) {
        stringBuilder.insert(offset, i);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, long l) {
        stringBuilder.insert(offset, l);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, float f) {
        stringBuilder.insert(offset, f);
        return this;
    }

    @Override
    public LevelStringBuilder insert(int offset, double d) {
        stringBuilder.insert(offset, d);
        return this;
    }


    @Override
    public int indexOf(String str) {
        return stringBuilder.indexOf(str);
    }

    @Override
    public int indexOf(String str, int fromIndex) {
        return stringBuilder.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(String str) {
        return stringBuilder.lastIndexOf(str);
    }

    @Override
    public int lastIndexOf(String str, int fromIndex) {
        return stringBuilder.lastIndexOf(str, fromIndex);
    }


    @Override
    public LevelStringBuilder reverse() {
        stringBuilder.reverse();
        return this;
    }
}

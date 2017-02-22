package microtrafficsim.utils.strings.builder;

/**
 * This interface defines all public methods of {@link java.lang.StringBuilder}.
 *
 * @author Dominic Parga Cacheiro
 */
public interface StringBuilder {

    StringBuilder append(Object obj);
    StringBuilder append(String str);
    StringBuilder append(StringBuffer sb);
    StringBuilder append(CharSequence s);
    StringBuilder append(CharSequence s, int start, int end);
    StringBuilder append(char[] str);
    StringBuilder append(char[] str, int offset, int len);
    StringBuilder append(boolean b);
    StringBuilder append(char c);
    StringBuilder append(int i);
    StringBuilder append(long lng);
    StringBuilder append(float f);
    StringBuilder append(double d);
    StringBuilder appendCodePoint(int codePoint);

    StringBuilder appendln();
    StringBuilder appendln(Object obj);
    StringBuilder appendln(String str);
    StringBuilder appendln(StringBuffer sb);
    StringBuilder appendln(CharSequence s);
    StringBuilder appendln(CharSequence s, int start, int end);
    StringBuilder appendln(char[] str);
    StringBuilder appendln(char[] str, int offset, int len);
    StringBuilder appendln(boolean b);
    StringBuilder appendln(char c);
    StringBuilder appendln(int i);
    StringBuilder appendln(long lng);
    StringBuilder appendln(float f);
    StringBuilder appendln(double d);
    StringBuilder appendlnCodePoint(int codePoint);

    StringBuilder delete(int start, int end);
    StringBuilder deleteCharAt(int index);

    StringBuilder replace(int start, int end, String str);

    StringBuilder insert(int index, char[] str, int offset, int len);
    StringBuilder insert(int offset, Object obj);
    StringBuilder insert(int offset, String str);
    StringBuilder insert(int offset, char[] str);
    StringBuilder insert(int dstOffset, CharSequence s);
    StringBuilder insert(int dstOffset, CharSequence s, int start, int end);
    StringBuilder insert(int offset, boolean b);
    StringBuilder insert(int offset, char c);
    StringBuilder insert(int offset, int i);
    StringBuilder insert(int offset, long l);
    StringBuilder insert(int offset, float f);
    StringBuilder insert(int offset, double d);

    int indexOf(String str);
    int indexOf(String str, int fromIndex);
    int lastIndexOf(String str);
    int lastIndexOf(String str, int fromIndex);

    StringBuilder reverse();

    String toString();
}
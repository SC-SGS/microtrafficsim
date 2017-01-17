package microtrafficsim.osm.parser.base;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.osm.primitives.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A OpenStreetMap XML parser base, providing functionality for extracting
 * OpenStreetMap primitives (elements).
 *
 * @author Maximilian Luz
 */
public class ParserBase {

    private ParserBaseEventHandler handler;

    /**
     * Constructs a new {@code ParserBase} using the specified handler.
     *
     * @param handler the handler to be used for handling the parser-events.
     */
    public ParserBase(ParserBaseEventHandler handler) {
        this.handler = handler;
    }


    /**
     * Parses the input stream to extract OpenStreetMap primitives and call the
     * callback functions.
     *
     * @param in the {@code InputStream} to parse.
     * @throws XMLStreamException if the given XML-File is malformed.
     */
    public void parse(InputStream in) throws XMLStreamException, InterruptedException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader  = factory.createXMLStreamReader(in);

        handler.onStart();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                String name = reader.getLocalName();

                switch (name) {
                case "bounds":   parseBounds(reader);   break;
                case "node":     parseNode(reader);     break;
                case "way":      parseWay(reader);      break;
                case "relation": parseRelation(reader); break;
                }
            }

            if (Thread.interrupted())
                throw new InterruptedException();
        }

        handler.onEnd();
    }

    /**
     * Parses a {@code Bounds} object from the given {@code XMLStreamReader}
     * and calls the callback-function.
     *
     * @param reader the {@code XMLStreamReader} from which to read.
     * @throws XMLStreamException
     */
    private void parseBounds(XMLStreamReader reader) throws XMLStreamException {
        double minlon = Double.parseDouble(reader.getAttributeValue(null, "minlon"));
        double maxlon = Double.parseDouble(reader.getAttributeValue(null, "maxlon"));
        double minlat = Double.parseDouble(reader.getAttributeValue(null, "minlat"));
        double maxlat = Double.parseDouble(reader.getAttributeValue(null, "maxlat"));

        // skip to end of element
        int level = 1;
        while (level > 0 && reader.hasNext()) {
            switch (reader.next()) {
            case XMLStreamReader.END_ELEMENT:   level--; break;
            case XMLStreamReader.START_ELEMENT: level++; break;
            default: break;
            }
        }

        handler.onPrimitiveParsed(new Bounds(minlat, minlon, maxlat, maxlon));
    }

    /**
     * Parses a {@code Node} object from the given {@code XMLStreamReader} and
     * calls the callback-function.
     *
     * @param reader the {@code XMLStreamReader} from which to read.
     * @throws XMLStreamException
     */
    private void parseNode(XMLStreamReader reader) throws XMLStreamException {
        long    id      = Long.parseLong(reader.getAttributeValue(null, "id"));
        double  lat     = Double.parseDouble(reader.getAttributeValue(null, "lat"));
        double  lon     = Double.parseDouble(reader.getAttributeValue(null, "lon"));
        boolean visible = parseOptionalBoolean(reader.getAttributeValue(null, "visible"), true);

        HashMap<String, String> tags = new HashMap<>();

        int level = 1;
        while (reader.hasNext() && level > 0) {
            switch (reader.next()) {
            case XMLStreamReader.START_ELEMENT:
                level++;
                if (reader.getLocalName().equals("tag")) parseTag(reader, tags);
                break;

            case XMLStreamReader.END_ELEMENT: level--; break;

            default: break;
            }
        }

        handler.onPrimitiveParsed(new Node(id, lat, lon, visible, tags));
    }

    /**
     * Parses a {@code Way} object from the given {@code XMLStreamReader} and
     * calls the callback-function.
     *
     * @param reader the {@code XMLStreamReader} from which to read.
     * @throws XMLStreamException
     */
    private void parseWay(XMLStreamReader reader) throws XMLStreamException {
        long    id      = Long.parseLong(reader.getAttributeValue(null, "id"));
        boolean visible = parseOptionalBoolean(reader.getAttributeValue(null, "visible"), true);

        ArrayList<Long> nodes = new ArrayList<>();
        HashMap<String, String> tags = new HashMap<>();

        int level = 0;
        while (reader.hasNext() && level >= 0) {
            switch (reader.next()) {
            case XMLStreamReader.START_ELEMENT:
                level++;

                String name = reader.getLocalName();
                if (name.equals("nd"))
                    nodes.add(Long.parseLong(reader.getAttributeValue(null, "ref")));
                else if (name.equals("tag"))
                    parseTag(reader, tags);

                break;

            case XMLStreamReader.END_ELEMENT: level--; break;
            }
        }

        handler.onPrimitiveParsed(new Way(id, visible, nodes, tags));
    }

    /**
     * Parses a {@code Relation} object from the given {@code XMLStreamReader}
     * and calls the callback-function.
     *
     * @param reader the {@code XMLStreamReader} from which to read.
     * @throws XMLStreamException
     */
    private void parseRelation(XMLStreamReader reader) throws XMLStreamException {
        long    id      = Long.parseLong(reader.getAttributeValue(null, "id"));
        boolean visible = parseOptionalBoolean(reader.getAttributeValue(null, "visible"), true);

        ArrayList<RelationMember> members = new ArrayList<>();
        HashMap<String, String> tags = new HashMap<>();

        int level = 0;
        while (reader.hasNext() && level >= 0) {
            switch (reader.next()) {
            case XMLStreamReader.START_ELEMENT:
                level++;

                String name = reader.getLocalName();
                if (name.equals("member"))
                    members.add(parseRelationMember(reader));
                else if (name.equals("tag"))
                    parseTag(reader, tags);

                break;

            case XMLStreamReader.END_ELEMENT: level--; break;
            }
        }

        handler.onPrimitiveParsed(new Relation(id, visible, members, tags));
    }


    /**
     * Parse a {@code RelationMember} object from the given {@code
     * XMLStreamReader}.
     *
     * @param reader the {@code XMLStreamReader} from which to read.
     * @return the parsed {@code RelationMember}.
     * @throws XMLStreamException
     */
    private RelationMember parseRelationMember(XMLStreamReader reader) throws XMLStreamException {
        Primitive.Type type;

        switch (reader.getAttributeValue(null, "type")) {
        case "way":      type = Primitive.Type.WAY; break;
        case "relation": type = Primitive.Type.RELATION; break;
        case "node":
        default:         type = Primitive.Type.NODE; break;
        }

        long   ref  = Long.parseLong(reader.getAttributeValue(null, "ref"));
        String role = reader.getAttributeValue(null, "role");

        return new RelationMember(type, ref, role);
    }

    /**
     * Parse a OpenStreetMap tag from the given {@code XMLStreamReader} and
     * insert it in the given {@code Map}.
     *
     * @param reader the {@code XMLStreamReader} from which to read.
     * @param tags   the {@code Map} in which to insert the tag.
     * @throws XMLStreamException
     */
    private void parseTag(XMLStreamReader reader, Map<String, String> tags) throws XMLStreamException {
        String k = reader.getAttributeValue(null, "k");
        String v = reader.getAttributeValue(null, "obj1");
        tags.put(k, v);
    }


    /**
     * Parse an optional boolean from the given {@code String}.
     *
     * @param s   the {@code String} to parse.
     * @param def the default value used when the given {@code String} is
     *            empty.
     * @return the parsed boolean or the default value if {@code s} is {@code
     * null}.
     */
    private boolean parseOptionalBoolean(String s, boolean def) {
        if (s == null)
            return def;
        else
            return Boolean.parseBoolean(s);
    }
}

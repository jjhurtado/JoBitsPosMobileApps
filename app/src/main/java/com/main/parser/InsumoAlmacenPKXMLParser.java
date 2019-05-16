package com.main.parser;

import com.main.Insumo;
import com.main.InsumoAlmacenPK;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Jorge on 27/9/17.
 */

public class InsumoAlmacenPKXMLParser extends AbstractXmlParser<InsumoAlmacenPK> {

    private final String
            COD_ALMACEN = "almacencodAlmacen",
            COD_INSUMO = "insumocodInsumo";

    public InsumoAlmacenPKXMLParser() {
        super("insumoAlmacenPK", "insumoAlmacenPKs");

    }


    @Override
    public InsumoAlmacenPK readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ENTITY);
        InsumoAlmacenPK i;
        String cod_insumo = null,cod_almacen = null;


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(COD_INSUMO)) {
                cod_insumo = readString(parser, COD_INSUMO);
            } else if (name.equals(COD_ALMACEN)) {
                cod_almacen = readString(parser, COD_ALMACEN);
            }else {
                skip(parser);
            }
        }


        i = new InsumoAlmacenPK(cod_insumo,cod_almacen);

        return i;


    }

}

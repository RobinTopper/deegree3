//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.ops;

import static java.lang.Integer.parseInt;
import static org.deegree.services.i18n.Messages.get;

import java.util.Map;

import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.rendering.r2d.legends.LegendOptions;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.unevaluated.Style;

/**
 * <code>GetLegendGraphic</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetLegendGraphic {

    private Style style;

    private String format;

    private LegendOptions opts = new LegendOptions();

    private int width = -1, height = -1;

    /**
     * @param map
     * @param service
     * @throws OWSException
     */
    public GetLegendGraphic( Map<String, String> map, MapService service ) throws OWSException {
        String layer = map.get( "LAYER" );
        if ( layer == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "LAYER" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        Layer l = service.getLayer( layer );
        if ( l == null ) {
            throw new OWSException( get( "WMS.LAYER_NOT_KNOWN", layer ), OWSException.LAYER_NOT_DEFINED );
        }
        String s = map.get( "STYLE" );
        style = service.getStyles().getLegendStyle( layer, s );
        if ( style == null ) {
            throw new OWSException( get( "WMS.UNDEFINED_STYLE", s, layer ),OWSException.STYLE_NOT_DEFINED );
        }
        format = map.get( "FORMAT" );
        if ( format == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "FORMAT" ), OWSException.MISSING_PARAMETER_VALUE );
        }

        String w = map.get( "WIDTH" );
        if ( w != null ) {
            try {
                width = parseInt( w );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "WIDTH", w ), OWSException.INVALID_PARAMETER_VALUE );
            }
        }
        String h = map.get( "HEIGHT" );
        if ( h != null ) {
            try {
                height = parseInt( h );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "HEIGHT", h ), OWSException.INVALID_PARAMETER_VALUE );
            }
        }
        w = map.get( "BASEWIDTH" );
        if ( w != null ) {
            try {
                opts.baseWidth = parseInt( w );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "BASEWIDTH", w ), OWSException.INVALID_PARAMETER_VALUE );
            }
        }
        h = map.get( "BASEHEIGHT" );
        if ( h != null ) {
            try {
                opts.baseHeight = parseInt( h );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "BASEHEIGHT", h ), OWSException.INVALID_PARAMETER_VALUE );
            }
        }
        h = map.get( "TEXTSIZE" );
        if ( h != null ) {
            try {
                opts.textSize = parseInt( h );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "TEXTSIZE", h ), OWSException.INVALID_PARAMETER_VALUE );
            }
        }
        h = map.get( "SPACING" );
        if ( h != null ) {
            try {
                opts.spacing = parseInt( h );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "SPACING", h ), OWSException.INVALID_PARAMETER_VALUE );
            }
        }
    }

    /**
     * @return the style selected by the request
     */
    public Style getStyle() {
        return style;
    }

    /**
     * @return the image format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @return the desired width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the desired height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param w
     */
    public void setWidth( int w ) {
        width = w;
    }

    /**
     * @param h
     */
    public void setHeight( int h ) {
        height = h;
    }

    /**
     * @return the legend options
     */
    public LegendOptions getLegendOptions() {
        return opts;
    }

}

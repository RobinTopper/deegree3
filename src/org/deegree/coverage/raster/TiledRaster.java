//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.coverage.raster;

import java.util.Arrays;
import java.util.List;

import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.container.TileContainer;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;

/**
 * This class represents a tiled AbstractRaster.
 * 
 * A TiledRaster contains multiple non-overlapping (TODO verify this) AbstractRasters
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class TiledRaster extends AbstractRaster {

    private TileContainer tileContainer;

    private RasterDataInfo rasterDataInfo;

    /**
     * Creates a new TiledRaster with tiles from the given TileContainer
     * 
     * @param tileContainer
     *            wraps all tiles
     */
    public TiledRaster( TileContainer tileContainer ) {
        super();
        this.tileContainer = tileContainer;
    }

    /**
     * Returns the wrapper for all tiles.
     * 
     * @return The container for all tiles.
     */
    public TileContainer getTileContainer() {
        return tileContainer;
    }

    @Override
    public Envelope getEnvelope() {
        return tileContainer.getEnvelope();
    }

    @Override
    public RasterReference getRasterReference() {
        return tileContainer.getRasterReference();
    }

    // TODO: convert to TileContainer
    @Override
    public TiledRaster copy() {
        // TiledRaster result = new TiledRaster();
        // for ( AbstractRaster r : tiles ) {
        // result.addTile( r.copy() );
        // }
        // return result;
        throw new UnsupportedOperationException();
    }

    @Override
    public TiledRaster getSubRaster( Envelope env ) {
        return getSubRaster( env, null );
    }

    @Override
    public TiledRaster getSubRaster( Envelope env, BandType[] bands ) {
        if ( getEnvelope().equals( env ) && ( bands == null || Arrays.equals( bands, getRasterDataInfo().bandInfo ) ) ) {
            return this;
        }
        // use the default tile container.
        MemoryTileContainer resultTC = new MemoryTileContainer();
        TiledRaster result = new TiledRaster( resultTC );

        for ( AbstractRaster r : getTileContainer().getTiles( env ) ) {
            Geometry intersection = r.getEnvelope().getIntersection( env );

            if ( intersection != null ) {
                Envelope subsetEnv = intersection.getEnvelope();
                resultTC.addTile( r.getSubRaster( subsetEnv, bands ) );
            }
        }

        if ( resultTC.getRasterReference() == null ) {
            throw new IndexOutOfBoundsException( "no intersection between TiledRaster and requested subset" );
        }

        return result;
    }

    @Override
    public void setSubRaster( Envelope envelope, AbstractRaster source ) {
        List<AbstractRaster> interSectingTiles = getTileContainer().getTiles( envelope );
        if ( !interSectingTiles.isEmpty() ) {
            for ( AbstractRaster r : interSectingTiles ) {
                if ( r != null ) {
                    Geometry intersection = r.getEnvelope().getIntersection( envelope );
                    if ( intersection != null ) {
                        Envelope subsetEnv = intersection.getEnvelope();
                        r.setSubRaster( subsetEnv, source );
                    }
                }
            }
        }
    }

    @Override
    public void setSubRaster( double x, double y, AbstractRaster source ) {
        RasterReference srcREnv = source.getRasterReference();
        RasterReference dstREnv = new RasterReference( x, y, srcREnv.getXRes(), srcREnv.getYRes() );
        Envelope dstEnv = dstREnv.getEnvelope( source.getColumns(), source.getRows() );
        RasterData srcData = source.getAsSimpleRaster().getRasterData();
        SimpleRaster movedRaster = new SimpleRaster( srcData, dstEnv, dstREnv );
        setSubRaster( dstEnv, movedRaster );
    }

    @Override
    public void setSubRaster( double x, double y, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubRaster( Envelope env, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {

        Envelope env = getEnvelope();
        List<AbstractRaster> tiles = getTileContainer().getTiles( env );
        if ( tiles == null || tiles.isEmpty() ) {
            throw new NullPointerException( "The given tile container does not contain any tiles. " );
        }
        SimpleRaster originalSimpleRaster = tiles.get( 0 ).getAsSimpleRaster();
        SimpleRaster result = originalSimpleRaster.createCompatibleSimpleRaster( getRasterReference(), env );

        for ( AbstractRaster r : tiles ) {
            Geometry intersec = r.getEnvelope().getIntersection( env );
            if ( intersec != null ) {
                if ( intersec instanceof Point ) {
                    continue;
                }
                Envelope subsetEnv = intersec.getEnvelope();
                result.setSubRaster( subsetEnv, r );
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "TiledRaster: " + getEnvelope() );
        result.append( "\n\t" );
        result.append( getTileContainer().toString() );
        return result.toString();
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        if ( rasterDataInfo == null ) {
            Envelope env = getEnvelope();
            double[] min = env.getMin().getAsArray();
            double[] max = new double[min.length];

            for ( int i = 0; i < min.length; ++i ) {
                max[i] = min[i] + 0.01;
            }

            Envelope tEnv = new GeometryFactory().createEnvelope( min, max, env.getCoordinateSystem() );

            List<AbstractRaster> tiles = getTileContainer().getTiles( tEnv );
            if ( !tiles.isEmpty() ) {
                SimpleRaster originalSimpleRaster = tiles.get( 0 ).getAsSimpleRaster();
                rasterDataInfo = originalSimpleRaster.getRasterDataInfo();
            }
        }
        return rasterDataInfo;
    }

}

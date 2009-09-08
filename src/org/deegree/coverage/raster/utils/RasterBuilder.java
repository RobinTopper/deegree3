//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.raster.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.IndexedMemoryTileContainer;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;

/**
 * The <code>RasterBuilder</code> recursively enters a given directory and creates a {@link TiledRaster} from found
 * image files.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterBuilder {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( RasterBuilder.class );

    /**
     * @param resolutionDirectory
     *            locating the different resolutions
     * @param extension
     *            to scan the directories for
     * @param recursive
     *            if the sub directories of the resolution directories should be scanned as well
     * @param crs
     *            in which the files are supposed to be defined
     * @return a {@link MultiResolutionRaster} filled with {@link TiledRaster}s or <code>null</code> if the
     *         resolutionDirectory is not a directory.
     */
    public static MultiResolutionRaster buildMultiResolutionRaster( File resolutionDirectory, String extension,
                                                                    boolean recursive, CRS crs ) {
        if ( !resolutionDirectory.isDirectory() ) {
            return null;
        }
        return buildMultiResolutionRaster( findResolutionDirs( resolutionDirectory ), extension, recursive, crs );
    }

    /**
     * Scan the given directory for top level directories ending with a resolution.
     * 
     * @param toplevelDir
     * @return a list of directories which can be used for the building of {@link TiledRaster}s.
     */
    private final static List<File> findResolutionDirs( File toplevelDir ) {
        List<File> result = new LinkedList<File>();
        for ( File f : toplevelDir.listFiles() ) {
            if ( f.isDirectory() ) {
                double res = RasterBuilder.getPixelResolution( null, f );
                if ( !Double.isNaN( res ) ) {
                    result.add( f );
                } else {
                    LOG.info( "Skipping directory: " + f.getAbsolutePath()
                              + "  because it does not denote a resolution." );
                }
            }
        }
        return result;
    }

    /**
     * @param resolutionDirectories
     *            locating the different resolutions
     * @param extension
     *            to scan the directories for
     * @param recursive
     *            if the sub directories of the resolution directories should be scanned as well
     * @param crs
     *            in which the files are supposed to be defined
     * @return a {@link MultiResolutionRaster} filled with {@link TiledRaster}s
     */
    public static MultiResolutionRaster buildMultiResolutionRaster( List<File> resolutionDirectories, String extension,
                                                                    boolean recursive, CRS crs ) {
        MultiResolutionRaster mrr = new MultiResolutionRaster();
        for ( File resDir : resolutionDirectories ) {
            if ( resDir != null && resDir.isDirectory() ) {
                AbstractRaster rasterLevel = RasterBuilder.buildTiledRaster( resDir, extension, recursive, crs );
                if ( rasterLevel != null ) {
                    mrr.addRaster( rasterLevel );
                }
            }
        }
        return mrr;
    }

    /**
     * Get the resolution from the resolution or if no value was configured try to get it from the name of the
     * directory.
     * 
     * @param resolution
     * @param resolutionDir
     * 
     * @return the resolution from the configuration if missing from the directory name, if not parse-able return NaN
     */
    public static double getPixelResolution( Double resolution, File resolutionDir ) {
        Double result = resolution;
        if ( result == null || result.isNaN() ) {
            File rasterDirectory = resolutionDir;
            String dirRes = FileUtils.getFilename( rasterDirectory );
            try {
                result = Double.parseDouble( dirRes );
            } catch ( NumberFormatException e ) {
                LOG.warn( "No resolution found in raster datasource defintion, nor in the directory name: " + dirRes
                          + " returning 0" );
                result = Double.NaN;
            }
        }
        return result;
    }

    /**
     * Scan the given directory (recursively) for files with given extension and create a tiled raster from them. The
     * tile raster will use an {@link IndexedMemoryTileContainer}.
     * 
     * @param directory
     * @param extension
     *            case sensitive extension of the files to to scan for, e.g jpg != JPG
     * @param recursive
     *            if true sub directories will be scanned as well.
     * @param crs
     *            in which the files are supposed to be defined
     * @return a new {@link TiledRaster} or <code>null</code> if no raster files were found at the given location,
     *         with the given extension.
     */
    public static AbstractRaster buildTiledRaster( File directory, String extension, boolean recursive, CRS crs ) {
        LOG.info( "Scanning for files in directory: {}", directory.getAbsolutePath() );
        List<File> coverageFiles = FileUtils.findFilesForExtensions( directory, recursive, extension );
        TiledRaster raster = null;
        if ( !coverageFiles.isEmpty() ) {
            List<AbstractRaster> rasters = new ArrayList<AbstractRaster>( coverageFiles.size() );
            QTreeInfo inf = buildTiledRaster( crs, coverageFiles, rasters );
            Envelope domain = inf.envelope;
            RasterReference rasterDomain = inf.rasterGeoReference;
            IndexedMemoryTileContainer container = new IndexedMemoryTileContainer( domain, rasterDomain,
                                                                                   inf.numberOfObjects );
            raster = new TiledRaster( container );
            raster.setCoordinateSystem( domain.getCoordinateSystem() );
            container.addRasterTiles( rasters );
        }
        return raster;
    }

    /**
     * 
     * @param coverageFiles
     *            to read
     * @param result
     *            will hold the resulting coverages.
     * @return the total envelope of the given coverages
     */
    private final static QTreeInfo buildTiledRaster( CRS crs, List<File> coverageFiles, List<AbstractRaster> result ) {
        Envelope resultEnvelope = null;
        RasterReference rasterReference = null;
        if ( crs == null ) {
            LOG.warn( "Configured crs is null, maybe the rasterfiles define one." );
        }
        CRS defaultCRS = crs;
        Envelope rasterEnvelope = null;
        for ( File filename : coverageFiles ) {
            try {
                AbstractRaster raster = RasterFactory.loadRasterFromFile( filename );
                CRS rasterCRS = raster.getCoordinateSystem();
                if ( defaultCRS == null ) {
                    defaultCRS = rasterCRS;
                } else {
                    if ( rasterCRS != null ) {
                        if ( !rasterCRS.equals( defaultCRS ) ) {
                            LOG.warn( "Configured CRS was not compatible with CRS in files, replacing it." );
                            defaultCRS = rasterCRS;
                        }
                    }
                }
                if ( rasterEnvelope == null ) {
                    rasterEnvelope = raster.getEnvelope();
                }
                if ( defaultCRS != null && raster.getCoordinateSystem() == null ) {
                    raster.setCoordinateSystem( defaultCRS );
                }
                if ( resultEnvelope == null ) {
                    resultEnvelope = raster.getEnvelope();
                } else {
                    resultEnvelope = resultEnvelope.merge( raster.getEnvelope() );
                }
                if ( rasterReference == null ) {
                    rasterReference = raster.getRasterReference();
                } else {
                    rasterReference.merger( raster.getRasterReference() );
                }
                result.add( raster );
            } catch ( IOException e ) {
                LOG.error( "unable to load raster, ignoring file ({}): {}", filename, e.getMessage() );
            }
        }
        int leafObjects = calcBalancedLeafObjectSize( rasterEnvelope, resultEnvelope, 4 );
        return new QTreeInfo( resultEnvelope, rasterReference, leafObjects );
    }

    /**
     * Calculate the approximate objects in a leaf node.
     * 
     * @param rasterEnvelope
     * @param resultEnvelope
     * @param size
     * @return
     */
    private static int calcBalancedLeafObjectSize( Envelope rasterEnvelope, Envelope resultEnvelope, int treeDepth ) {
        double tw = resultEnvelope.getWidth();

        double rw = rasterEnvelope.getWidth();

        double widthScale = Math.pow( 0.5, treeDepth-- );

        double leafSize = tw * widthScale;
        while ( leafSize < ( 5 * rw ) ) {
            widthScale = Math.pow( 0.5, treeDepth-- );
            leafSize = tw * widthScale;
        }

        return Math.max( 3, (int) Math.ceil( leafSize / rw ) );
    }

    private static class QTreeInfo {
        Envelope envelope;

        RasterReference rasterGeoReference;

        int numberOfObjects;

        /**
         * @param envelope
         * @param rasterGeoReference
         * @param numberOfObjects
         */
        public QTreeInfo( Envelope envelope, RasterReference rasterGeoReference, int numberOfObjects ) {
            this.envelope = envelope;
            this.rasterGeoReference = rasterGeoReference;
            this.numberOfObjects = numberOfObjects;
        }

    }
}

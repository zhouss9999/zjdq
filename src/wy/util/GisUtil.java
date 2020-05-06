package wy.util;


import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

/**
 * @author troy
 * @date 2018/9/26
 * @description
 */

public class GisUtil {

    private static String LambertToWGS84 = "proj=lcc  datum=WGS84 lon_0=110 lat_1=25 lat_2=40 lat_0=34 x_0=0 y_0=0";

    private static String WGS84ToLambert = "proj=lcc  lon_0=110 lat_1=25 lat_2=40 lat_0=34 x_0=0 y_0=0";


    private static String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +datum=WGS84 +units=degrees";

    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    private static final CRSFactory csFactory = new CRSFactory();


    public static ProjCoordinate convertLambertTOWGS84(ProjCoordinate lambertProjCoordinate) {

        CoordinateReferenceSystem lambert = csFactory.createFromParameters(null, LambertToWGS84);
        CoordinateReferenceSystem WGS84 = lambert.createGeographic();
        CoordinateTransform trans = ctFactory.createTransform(lambert, WGS84);
        ProjCoordinate wgs84 = new ProjCoordinate();
        return trans.transform(lambertProjCoordinate, wgs84);

    }

    public static ProjCoordinate convertWGSToLambert(ProjCoordinate wgs84){
        CoordinateReferenceSystem WGS84 = csFactory.createFromParameters(null,WGS84_PARAM );
        CoordinateReferenceSystem lambert = csFactory.createFromParameters(null, WGS84ToLambert);
        CoordinateTransform trans = ctFactory.createTransform(WGS84, lambert);
        ProjCoordinate pout = new ProjCoordinate();
        return trans.transform(wgs84, pout);

    }

    public static void main(String[] args) {
		System.out.println(convertWGSToLambert(new ProjCoordinate(120.152032,37.510641)).toShortString());
		System.out.println(convertLambertTOWGS84(new ProjCoordinate(734000,376000)).toShortString());
	}
}

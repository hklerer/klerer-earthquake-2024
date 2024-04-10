package klerer.earthquake;

import io.reactivex.rxjava3.core.Single;
import klerer.earthquake.json.FeatureCollection;
import retrofit2.http.GET;

public interface EarthquakeService {
    @GET("/earthquakes/feed/v1.0/summary/1.0_hour.geojson")
    Single<FeatureCollection> oneHour();

}

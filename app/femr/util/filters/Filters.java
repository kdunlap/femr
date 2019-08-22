package femr.util.filters;

import com.google.inject.Inject;
import femr.util.filters.jwt.JwtFilter;
import play.filters.cors.CORSFilter;
import play.http.DefaultHttpFilters;

public class Filters extends DefaultHttpFilters {

    @Inject
    public Filters(CORSFilter corsFilter, JwtFilter jwtFilter, ResearchFilter researchFilter){

        super(corsFilter, jwtFilter, researchFilter);
    }
}

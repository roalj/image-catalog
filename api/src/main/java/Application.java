import com.kumuluz.ee.discovery.annotations.RegisterService;
import resource.ImageResource;

import javax.ws.rs.ApplicationPath;
import java.util.Collections;
import java.util.Set;

@RegisterService
@ApplicationPath("api")
public class Application extends javax.ws.rs.core.Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(ImageResource.class);
    }
}

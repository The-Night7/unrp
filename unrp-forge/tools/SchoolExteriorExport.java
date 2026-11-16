import com.myriam.unrp.worldgen.*;
import java.io.*;
import java.nio.file.*;

/** Export the exact production plan for the native exterior preview renderer. */
public class SchoolExteriorExport {
    public static void main(String[] args) throws Exception {
        Path out=Path.of(args[0]);Files.createDirectories(out);
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);var grid=new SchoolPlanGrid(plan);
            try(var stream=new DataOutputStream(Files.newOutputStream(out.resolve(school.id()+".bin")))) {
                stream.writeInt(plan.size());stream.writeInt(plan.height());
                for(int y=0;y<plan.height();y++)for(int z=0;z<plan.size();z++)for(int x=0;x<plan.size();x++)stream.writeByte(grid.at(x,y,z).ordinal());
            }
            var colors=new StringBuilder();
            for(var m:SchoolBlueprint.Material.values())colors.append(m.name()).append(' ').append(SchoolPreview.color(m,school)).append('\n');
            Files.writeString(out.resolve(school.id()+".palette"),colors);
        }
    }
}

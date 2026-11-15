import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** Floor-by-floor schematic, generated from the same geometry as world generation. */
public class WellstonPreview {
    public static void main(String[] args) throws Exception {
        var school=SchoolSites.school("wellston");var plan=SchoolBlueprint.create(school);
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1180' height='1320'><rect width='1180' height='1320' fill='#17140e'/><style>rect{shape-rendering:crispEdges}text{font-family:monospace;fill:#f1d586}</style><text x='26' y='32' font-size='23'>Wellston Private High School — Minecraft campus</text><text x='26' y='56' font-size='13'>Canon-based locations · adapted room arrangement · 129 × 129 blocks · north at top</text>");
        String[] names={"Ground floor / dormitory ground floor","Second floor / dormitory upper floor","Third floor","Accessible main rooftop"};
        String[] notes={"Infirmary, headmaster, library, cafeteria, separate boys/girls dormitories","Safe House: Room 224 · Wellston Weekly press room","Classrooms and study rooms · main stairs continue to roof","Flat roof, safety fence and service hut · no clock tower"};
        for(int i=0;i<4;i++) {
            int x=26+(i%2)*584,y=96+(i/2)*594;
            svg.append("<text x='").append(x).append("' y='").append(y).append("' font-size='16'>").append(names[i]).append("</text>");
            SchoolPreview.floor(svg,plan,school,i*6,x,y+14,520);
            svg.append("<text x='").append(x).append("' y='").append(y+558).append("' font-size='10'>").append(notes[i]).append("</text>");
        }
        svg.append("<text x='26' y='1304' font-size='12'>Terracotta / pale trim exterior; lockers, furniture and room signs in game. Exact dimensions are not canonical.</text></svg>");
        Files.writeString(Path.of(args[0]),svg);
    }
}

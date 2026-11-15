import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** Floor plans for the four remaining dedicated campus designs. */
public class CampusPreview {
    public static void main(String[] args) throws Exception {
        var school=SchoolSites.school(args[0]);var plan=SchoolBlueprint.create(school);
        int[] floors=switch(school.id()) {case "westview" -> new int[]{0,6,12,18,24};case "wellston_university" -> new int[]{0,6,12,18,42,48};default -> new int[]{0,6,12,18};};
        String description=switch(school.id()) {
            case "new_bostin" -> "Canon-informed brick exterior, rear walkways, classroom 2-B and roof; room positions adapted.";
            case "rowden" -> "Original campus for a canon school; its architectural plan is not established by the references.";
            case "westview" -> "Photo-inspired concrete/glass campus, blue-grey roofs and covered walks; scale and interiors adapted.";
            default -> "Episode 369-inspired mixed-height campus and medical teaching lab; scale and room positions adapted.";
        };
        int rows=(floors.length+1)/2,height=120+rows*580;
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1180' height='"+height+"'><rect width='1180' height='"+height+"' fill='#17140e'/><style>rect{shape-rendering:crispEdges}text{font-family:monospace;fill:#f1d586}</style><text x='26' y='32' font-size='23'>"+school.name()+" — Minecraft campus</text><text x='26' y='56' font-size='12'>"+description+"</text>");
        for(int i=0;i<floors.length;i++) {
            int x=26+(i%2)*584,y=96+(i/2)*580;
            String title=floors[i]==0?"Ground floor":floors[i]==6?"Second level":floors[i]==12?"Third level / lower roofs":"Level at Y +"+floors[i];
            svg.append("<text x='").append(x).append("' y='").append(y).append("' font-size='16'>").append(title).append("</text>");
            SchoolPreview.floor(svg,plan,school,floors[i],x,y+14,520);
        }
        svg.append("<text x='26' y='").append(height-24).append("' font-size='12'>").append(plan.size()).append(" × ").append(plan.size()).append(" blocks · one natural site per Overworld · exact dimensions and unseen rooms are adaptations</text></svg>");
        Files.writeString(Path.of(args[1]),svg);
    }
}

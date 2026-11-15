import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** Run with the two pure worldgen planner classes on the classpath. */
public class SchoolPreview {
    public static void main(String[] args) throws Exception {
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1100' height='860' viewBox='0 0 1100 860'><rect width='1100' height='860' fill='#17140e'/><style>text{font-family:monospace;fill:#f1d586}</style><text x='28' y='30' font-size='21'>UNRP — procedural school campuses</text><text x='28' y='52' font-size='12'>Ground-floor plan · one seed-based site per school · 73 × 73 blocks</text>");
        int index=0;
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);
            int panelX=28+(index%3)*358,panelY=82+(index/3)*382;
            svg.append("<text x='").append(panelX).append("' y='").append(panelY).append("' font-size='16'>").append(school.name()).append(" · ").append(school.floors()).append(" floors</text>");
            var cells=new SchoolBlueprint.Material[4][73][73];
            for(var layer:cells) for(var row:layer) java.util.Arrays.fill(row,SchoolBlueprint.Material.AIR);
            for(var box:plan.boxes()) for(int y=Math.max(0,box.y1());y<=Math.min(3,box.y2());y++) for(int x=box.x1();x<=box.x2();x++) for(int z=box.z1();z<=box.z2();z++) cells[y][x][z]=box.material();
            for(int x=0;x<73;x++) for(int z=0;z<73;z++) {
                var material=cells[1][x][z];
                if(material==SchoolBlueprint.Material.AIR) material=cells[0][x][z];
                if(cells[2][x][z]==SchoolBlueprint.Material.GLASS) material=SchoolBlueprint.Material.GLASS;
                String color=switch(material) {
                    case GRASS -> "#35543c"; case WALL,FOUNDATION -> "#d5cbb2";
                    case FLOOR -> "#96714e"; case GLASS,WATER -> "#73b5c9";
                    case ACCENT -> switch(school.palette()) { case "blue" -> "#455b9f"; case "green" -> "#528153"; case "red" -> "#ac5142"; case "cyan" -> "#459ba0"; case "purple" -> "#8b689c"; default -> "#d3ac41"; };
                    case COURT -> "#b7633b"; case WHITE -> "#eee7d3"; case FENCE -> "#68676c";
                    case PAVING -> "#707570"; case BOOKS,LOG -> "#654830"; case LEAVES -> "#476846";
                    case DOOR_LOWER,DOOR_UPPER -> "#c8a467"; case BED_FOOT,BED_HEAD -> "#dfdce7";
                    case LIGHT -> "#fff3a0"; case DESK,CHAIR,COUNTER -> "#c9a47a"; case STAIRS -> "#b1a799";
                    default -> "#4e4e50";
                };
                svg.append("<rect x='").append(panelX+x*4).append("' y='").append(panelY+12+z*4).append("' width='4' height='4' fill='").append(color).append("'/>");
            }
            svg.append("<text x='").append(panelX).append("' y='").append(panelY+326).append("' font-size='11'>Entrance at top · classrooms / offices</text><text x='").append(panelX).append("' y='").append(panelY+342).append("' font-size='11'>Cafeteria left · gym right · court below</text>");
            index++;
        }
        svg.append("<text x='28' y='846' font-size='12'>Original layouts, not canonical replicas. Upper floors include a library, labs and teaching rooms.</text></svg>");
        Files.writeString(Path.of(args[0]),svg);
    }
}

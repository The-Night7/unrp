import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** Standalone vector previews from the exact procedural geometry. */
public class SchoolPreview {
    public static void main(String[] args) throws Exception {
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1100' height='860' viewBox='0 0 1100 860'><rect width='1100' height='860' fill='#17140e'/><style>rect{shape-rendering:crispEdges}text{font-family:monospace;fill:#f1d586}</style><text x='28' y='30' font-size='21'>UNRP — procedural school campuses</text><text x='28' y='52' font-size='12'>Ground floor · one site per school · Wellston 129 × 129; other campuses 73 × 73</text>");
        int index=0;
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);
            int x=28+(index%3)*358,y=82+(index/3)*382;
            svg.append("<text x='").append(x).append("' y='").append(y).append("' font-size='16'>").append(school.name()).append(" · ").append(school.floors()).append(" floors</text>");
            floor(svg,plan,school,0,x,y+12,292);
            String a=school.id().equals("wellston")?"Three teaching blocks · separate dormitories":"Entrance at top · classrooms / offices";
            String b=school.id().equals("wellston")?"Room 224 and roof shown in Wellston preview":"Cafeteria left · gym right · court below";
            svg.append("<text x='").append(x).append("' y='").append(y+326).append("' font-size='11'>").append(a).append("</text><text x='").append(x).append("' y='").append(y+342).append("' font-size='11'>").append(b).append("</text>");
            index++;
        }
        svg.append("<text x='28' y='846' font-size='12'>Wellston is canon-based with adapted dimensions/adjacencies. Other school layouts remain original.</text></svg>");
        Files.writeString(Path.of(args[0]),svg);
    }
    public static void floor(StringBuilder svg,SchoolBlueprint.Plan plan,SchoolSites.School school,int floor,double px,double py,double width) {
        int size=plan.size();
        var cells=new SchoolBlueprint.Material[3][size][size];
        for(var layer:cells) for(var row:layer) java.util.Arrays.fill(row,SchoolBlueprint.Material.AIR);
        for(var box:plan.boxes()) for(int y=Math.max(floor,box.y1());y<=Math.min(floor+2,box.y2());y++)
            for(int x=box.x1();x<=box.x2();x++) for(int z=box.z1();z<=box.z2();z++) cells[y-floor][x][z]=box.material();
        double scale=width/size;
        for(int x=0;x<size;x++) {
            String previous="";int start=0;
            for(int z=0;z<=size;z++) {
                String color="";
                if(z<size) {
                    var m=cells[1][x][z];if(m==SchoolBlueprint.Material.AIR) m=cells[0][x][z];
                    if(cells[2][x][z]==SchoolBlueprint.Material.GLASS) m=SchoolBlueprint.Material.GLASS;
                    color=color(m,school);
                }
                if(!color.equals(previous)) {
                    if(z>start) svg.append(String.format(java.util.Locale.ROOT,"<rect x='%.2f' y='%.2f' width='%.2f' height='%.2f' fill='%s'/>",px+x*scale,py+start*scale,scale+0.02,(z-start)*scale+0.02,previous));
                    previous=color;start=z;
                }
            }
        }
    }
    private static String color(SchoolBlueprint.Material material,SchoolSites.School school) {
        return switch(material) {
            case AIR -> "#252624";case GRASS -> "#35543c";
            case WALL -> school.id().equals("wellston")?"#ad735d":"#d5cbb2";
            case FOUNDATION -> "#99978b";case PARTITION -> "#e4dfd3";case TRIM -> "#e6ceaa";
            case FLOOR -> "#b0936f";case GLASS,WATER -> "#73b5c9";
            case ACCENT -> switch(school.palette()) {case "blue" -> "#455b9f";case "green" -> "#528153";case "red" -> "#ac5142";case "cyan" -> "#459ba0";case "purple" -> "#8b689c";case "wellston" -> "#e6ceaa";default -> "#d3ac41";};
            case ROOF -> "#959d9e";case COURT -> "#b7633b";case WHITE -> "#eee7d3";case FENCE,LOCKER -> "#9da4aa";
            case PAVING -> "#707570";case BOOKS,LOG -> "#654830";case LEAVES -> "#476846";
            case DOOR_LOWER,DOOR_UPPER -> "#c8a467";case BED_FOOT,BED_HEAD -> "#dfdce7";
            case LIGHT -> "#fff3a0";case DESK,CHAIR,COUNTER -> "#c9a47a";case STAIRS -> "#b1a799";
            case CAFETERIA_CHAIR -> "#8a6da4";case CAFETERIA_TABLE -> "#eee8dc";case CAFETERIA_COUNTER -> "#73a39a";case CAFETERIA_WALL -> "#dfc16a";
            case SOFA -> "#a0aca6";case COMPUTER -> "#212f39";default -> "#4e4e50";
        };
    }
}

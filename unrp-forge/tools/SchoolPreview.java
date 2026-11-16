import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** Standalone vector previews from the exact procedural geometry. */
public class SchoolPreview {
    public static void main(String[] args) throws Exception {
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1100' height='860' viewBox='0 0 1100 860'><rect width='1100' height='860' fill='#17140e'/><style>rect{shape-rendering:crispEdges}text{font-family:monospace;fill:#f1d586}</style><text x='28' y='30' font-size='21'>UNRP — procedural school campuses</text><text x='28' y='52' font-size='12'>Distinct campuses · one natural site per school · footprints from 113 × 113 to 193 × 193 blocks</text>");
        int index=0;
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);
            int x=28+(index%3)*358,y=82+(index/3)*382;
            svg.append("<text x='").append(x).append("' y='").append(y).append("' font-size='16'>").append(school.name()).append(" · ").append(school.id().equals("wellston_university")?"up to "+school.floors():school.floors()).append(" floors</text>");
            floor(svg,plan,school,school.id().equals("agwin")?2:0,x,y+12,292);
            String a=switch(school.id()) {
                case "agwin" -> "Five-storey building · raised entrance";
                case "wellston" -> "Three teaching blocks · separate dormitories";
                case "new_bostin" -> "Brick teaching block · rear balconies";
                case "rowden" -> "Teaching, library/arts and commons pavilions";
                case "westview" -> "Eight glazed blocks · covered walkways";
                default -> "Medical Department · modern towers";
            };
            String b=switch(school.id()) {
                case "agwin" -> "Classroom 2-3 · windowed corridors";
                case "wellston" -> "Room 224 · press room · fenced rooftop";
                case "new_bostin" -> "Classroom 2-B · lockers · accessible roof";
                case "rowden" -> "Original architecture; no canonical map claim";
                case "westview" -> "Concrete gateway · leaf mural · open lawns";
                default -> "Medical labs · lectures · student residences";
            };
            svg.append("<text x='").append(x).append("' y='").append(y+326).append("' font-size='11'>").append(a).append("</text><text x='").append(x).append("' y='").append(y+342).append("' font-size='11'>").append(b).append("</text>");
            index++;
        }
        svg.append("<text x='28' y='846' font-size='12'>Canon references where available; unknown layouts and the original Westview design are documented adaptations.</text></svg>");
        Files.writeString(Path.of(args[0]),svg);
    }
    public static void floor(StringBuilder svg,SchoolBlueprint.Plan plan,SchoolSites.School school,int floor,double px,double py,double width) {
        int size=plan.size();
        var cells=new SchoolBlueprint.Material[3][size][size];
        for(var layer:cells) for(var row:layer) java.util.Arrays.fill(row,SchoolBlueprint.Material.AIR);
        for(var box:plan.boxes()) for(int y=Math.max(floor,box.y1());y<=Math.min(floor+2,box.y2());y++)
            for(int x=box.x1();x<=box.x2();x++) for(int z=box.z1();z<=box.z2();z++) cells[y-floor][x][z]=box.material();
        var ground=new SchoolBlueprint.Material[size][size];
        for(var row:ground) java.util.Arrays.fill(row,SchoolBlueprint.Material.AIR);
        if(school.id().equals("agwin") && floor==2) for(var box:plan.boxes()) if(box.y1()<=0 && box.y2()>=0)
            for(int gx=box.x1();gx<=box.x2();gx++) for(int gz=box.z1();gz<=box.z2();gz++) ground[gx][gz]=box.material();
        double scale=width/size;
        for(int x=0;x<size;x++) {
            String previous="";int start=0;
            for(int z=0;z<=size;z++) {
                String color="";
                if(z<size) {
                    var m=cells[1][x][z];if(m==SchoolBlueprint.Material.AIR) m=cells[0][x][z];
                    if(m==SchoolBlueprint.Material.AIR && school.id().equals("agwin") && floor==2) m=ground[x][z];
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
    public static String color(SchoolBlueprint.Material material,SchoolSites.School school) {
        return switch(material) {
            case AIR -> "#252624";case GRASS -> "#35543c";
            case WALL -> switch(school.palette()) {
                case "new_bostin" -> "#8d6651";case "rowden" -> "#94979a";case "westview", "westview_modern" -> "#ece8df";
                case "university" -> "#aa7b64";case "agwin" -> "#e3e4de";case "wellston" -> "#ad735d";default -> "#d5cbb2";
            };
            case FOUNDATION -> "#99978b";case PARTITION -> "#e4dfd3";case TRIM -> java.util.Set.of("agwin","new_bostin","rowden","university","westview_modern").contains(school.palette())?"#a6adad":"#e6ceaa";
            case FLOOR -> java.util.Set.of("agwin","new_bostin","university","westview_modern").contains(school.palette())?"#bcc0bb":"#b0936f";case GLASS -> java.util.Set.of("agwin","new_bostin","university","westview_modern").contains(school.palette())?"#d6dfe0":"#73b5c9";case WATER -> "#73b5c9";
            case ACCENT -> switch(school.palette()) {case "blue" -> "#455b9f";case "green" -> "#528153";case "red" -> "#ac5142";case "cyan" -> "#459ba0";case "purple" -> "#8b689c";case "new_bostin" -> "#9ba0a0";case "rowden" -> "#984c43";case "westview_modern" -> "#adaeaa";case "westview" -> "#4697a2";case "university" -> "#dddfe0";case "agwin" -> "#9fa7a4";case "wellston" -> "#e6ceaa";default -> "#d3ac41";};
            case ROOF -> switch(school.palette()) {case "westview_modern" -> "#798e9e";case "westview" -> "#528f8a";case "rowden" -> "#3c464c";case "university" -> "#727978";case "agwin" -> "#898f84";default -> "#959d9e";};case COURT -> "#b7633b";case WHITE -> "#eee7d3";case FENCE,LOCKER -> "#9da4aa";
            case PAVING -> "#707570";case BOOKS,LOG -> "#654830";case LEAVES -> "#476846";
            case DOOR_LOWER,DOOR_UPPER -> "#c8a467";case BED_FOOT,BED_HEAD -> "#dfdce7";
            case LIGHT -> "#fff3a0";case DESK,CHAIR,COUNTER -> "#c9a47a";case STAIRS -> "#b1a799";
            case CAFETERIA_CHAIR -> "#8a6da4";case CAFETERIA_TABLE -> "#eee8dc";case CAFETERIA_COUNTER -> "#73a39a";case CAFETERIA_WALL -> "#dfc16a";
            case AGWIN_YELLOW -> "#e0d3a1";case AGWIN_PINK -> "#cfb0a8";case CENTRE_PANEL -> "#eeefea";
            case WINDOW_FRAME,WINDOW_MULLION -> "#303a3c";case HALL_TRIM -> "#94aba3";
            case SIDE_DOOR_LOWER,SIDE_DOOR_UPPER -> "#d7d7c9";case STAIRS_NORTH -> "#b1a799";
            case TOWER_WALL -> "#c8cdce";case TOWER_GLASS -> "#7e8c94";case BREWING_STAND -> "#c4b66c";
            case ROAD_DARK -> "#45464b";case PLAZA_PAVERS -> "#a3a7a7";case GATE_EW,GATE_NS -> "#aab1b3";case MURAL_GREEN -> "#44733d";case MURAL_LIGHT -> "#7b8b43";case FLAG_CYAN -> "#25888a";
            case WINDOW_SILL,CORNICE -> "#cbc9bc";case PATH_EDGE -> "#81817a";case PATH_TILE -> "#aaa9a3";case PLANTER_SOIL -> "#6b533d";case SHRUB -> "#56824b";case FLOWER -> "#e1bd42";case FACADE_BASE -> "#858781";case FACADE_PIER -> "#d2ccba";case FLOOR_INLAY -> "#6c6355";case PENDANT -> "#edba63";
            case STREET_POST -> "#5b4b38";case STREET_LANTERN -> "#e7bc69";case ROOF_VENT -> "#7d807c";
            case SOFA -> "#a0aca6";case COMPUTER -> "#212f39";default -> "#4e4e50";
        };
    }
}

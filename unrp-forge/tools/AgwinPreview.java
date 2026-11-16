import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** North elevation and six floor slices, directly from the generation geometry. */
public class AgwinPreview {
    public static void main(String[] args) throws Exception {
        var school=SchoolSites.school("agwin");var plan=SchoolBlueprint.create(school);
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1100' height='1320'><rect width='1100' height='1320' fill='#17140e'/><style>rect{shape-rendering:crispEdges}text{font-family:monospace;fill:#f1d586}</style><text x='26' y='30' font-size='23'>Agwin Private High School — Minecraft campus</text><text x='26' y='53' font-size='12'>Episode 374 exterior / corridor / classroom reference · other room positions are adaptations</text>");
        int size=plan.size(),height=plan.height(),layer=size*size;
        byte[] cells=new byte[layer*height];
        for(var box:plan.boxes()) for(int y=Math.max(0,box.y1());y<=box.y2();y++)
            for(int x=box.x1();x<=box.x2();x++) for(int z=box.z1();z<=box.z2();z++) cells[x+z*size+y*layer]=(byte)box.material().ordinal();
        var materials=SchoolBlueprint.Material.values();double scale=1010.0/size;
        for(int x=0;x<size;x++) for(int y=0;y<height;y++) {
            SchoolBlueprint.Material m=SchoolBlueprint.Material.AIR;
            for(int z=0;z<=70;z++) {
                var at=materials[cells[x+z*size+y*layer]];
                if(at!=SchoolBlueprint.Material.AIR && at!=SchoolBlueprint.Material.SIGN && at!=SchoolBlueprint.Material.WALL_SIGN) {m=at;break;}
            }
            if(m!=SchoolBlueprint.Material.AIR) svg.append(String.format(java.util.Locale.ROOT,"<rect x='%.2f' y='%.2f' width='%.2f' height='%.2f' fill='%s'/>",45+x*scale,78+(height-1-y)*scale,scale+0.02,scale+0.02,SchoolPreview.color(m,school)));
        }
        svg.append("<text x='26' y='507' font-size='12'>North elevation · pale projecting centre, pastel wings, broad canopy, metal gate</text>");
        String[] names={"Ground floor (raised plinth)","Second floor — classroom 2-3","Third floor","Fourth floor","Fifth floor","Roof / service penthouse"};
        for(int i=0;i<6;i++) {
            int x=26+(i%3)*358,y=550+(i/3)*356;
            svg.append("<text x='").append(x).append("' y='").append(y).append("' font-size='14'>").append(names[i]).append("</text>");
            SchoolPreview.floor(svg,plan,school,2+i*6,x,y+14,292);
        }
        svg.append("<text x='26' y='1281' font-size='12'>113 × 113 blocks · one Agwin campus per Overworld · dimensions and unseen interiors are adapted</text><text x='26' y='1302' font-size='12'>Support rooms and roof access are gameplay choices; no invented royal arena, beacon spire or Wellston club.</text></svg>");
        Files.writeString(Path.of(args[0]),svg);
    }
}

import com.myriam.unrp.worldgen.SchoolBlueprint;
import com.myriam.unrp.worldgen.SchoolSites;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exterior projections from the exact generated blocks, without Minecraft dependencies. */
public class WestviewPreview {
    public static void main(String[] args) throws Exception {
        var school=SchoolSites.school("westview");var plan=SchoolBlueprint.create(school);
        int size=plan.size(),height=plan.height(),layer=size*size;
        byte[] cells=new byte[layer*height];
        for(var box:plan.boxes()) for(int y=Math.max(0,box.y1());y<=Math.min(height-1,box.y2());y++)
            for(int x=box.x1();x<=box.x2();x++) for(int z=box.z1();z<=box.z2();z++) cells[x+z*size+y*layer]=(byte)box.material().ordinal();
        var materials=SchoolBlueprint.Material.values();
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='1100' height='1380'><rect width='1100' height='1380' fill='#17140e'/><style>rect{shape-rendering:crispEdges}text{font-family:monospace;fill:#f1d586}</style><text x='30' y='35' font-size='24'>Westview — photo-inspired campus</text><text x='30' y='62' font-size='13'>Concrete ends · glazed facades · blue-grey roofs · covered paths · green courtyards</text>");
        double scale=1020.0/size;
        for(int x=0;x<size;x++) for(int y=0;y<height;y++) {
            var m=SchoolBlueprint.Material.AIR;
            for(int z=0;z<size;z++) {var at=materials[cells[x+z*size+y*layer]];if(visible(at)){m=at;break;}}
            if(visible(m)) rect(svg,40+x*scale,80+(height-1-y)*scale,scale,SchoolPreview.color(m,school));
        }
        svg.append("<text x='30' y='302' font-size='14'>Entrance elevation — concrete gateway, leaf mural and glass reception</text><text x='30' y='348' font-size='17'>Roof / site plan (entrance at top)</text>");
        scale=940.0/size;
        for(int x=0;x<size;x++) for(int z=0;z<size;z++) {
            var m=SchoolBlueprint.Material.AIR;
            for(int y=height-1;y>=0;y--) {var at=materials[cells[x+z*size+y*layer]];if(visible(at)){m=at;break;}}
            rect(svg,80+x*scale,370+z*scale,scale,SchoolPreview.color(m,school));
        }
        svg.append("<text x='30' y='1340' font-size='13'>177 × 177 blocks · eight buildings · one natural Westview site per Overworld</text><text x='30' y='1364' font-size='12'>Geometry projection, not an in-game screenshot. Scale and furnished interiors are adaptations.</text></svg>");
        Files.writeString(Path.of(args[0]),svg);
    }
    private static boolean visible(SchoolBlueprint.Material m) {return m!=SchoolBlueprint.Material.AIR && m!=SchoolBlueprint.Material.SIGN && m!=SchoolBlueprint.Material.WALL_SIGN;}
    private static void rect(StringBuilder svg,double x,double y,double size,String color) {svg.append(String.format(java.util.Locale.ROOT,"<rect x='%.2f' y='%.2f' width='%.2f' height='%.2f' fill='%s'/>",x,y,size+.02,size+.02,color));}
}

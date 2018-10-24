import processing.core.PApplet;
import processing.core.PImage;

import java.util.Optional;

final class WorldView
{
   private PApplet screen;
   private WorldModel world;
   private int tileWidth;
   private int tileHeight;
   private Viewport viewport;

   public WorldView(int numRows, int numCols, PApplet screen, WorldModel world,
      int tileWidth, int tileHeight)
   {
      this.screen = screen;
      this.world = world;
      this.tileWidth = tileWidth;
      this.tileHeight = tileHeight;
      this.viewport = new Viewport(numRows, numCols);
   }

   public void shiftView(int colDelta, int rowDelta) {
      int newCol = Functions.clamp(this.viewport.col + colDelta, 0,
              this.world.numCols - this.viewport.numCols);
      int newRow = Functions.clamp(this.viewport.row + rowDelta, 0,
              this.world.numRows - this.viewport.numRows);

      this.viewport.shift(newCol, newRow);
   }

   private void drawBackground() {
      for (int row = 0; row < this.viewport.numRows; row++) {
         for (int col = 0; col < this.viewport.numCols; col++) {
            Point worldPoint = this.viewport.viewportToWorld(col, row);
            Optional<PImage> image = this.world.getBackgroundImage(worldPoint);
            if (image.isPresent()) {
               this.screen.image(image.get(), col * this.tileWidth,
                       row * this.tileHeight);
            }
         }
      }
   }

   private void drawEntities() {
      for (Entity entity : this.world.entities) {
         Point pos = entity.position;

         if (this.viewport.contains(pos)) {
            Point viewPoint = this.viewport.worldToViewport(pos.x, pos.y);
             this.screen.image(Functions.getCurrentImage(entity),
                    viewPoint.x * this.tileWidth, viewPoint.y * this.tileHeight);
         }
      }
   }

   public void drawViewport() {
      drawBackground();
      drawEntities();
   }
}

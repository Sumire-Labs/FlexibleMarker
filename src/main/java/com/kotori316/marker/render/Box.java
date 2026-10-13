package com.kotori316.marker.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

/** Direct Java port of Kotori316's Box.scala, preserving vertex order and texture coordinates. */
public class Box {
    public final double startX, startY, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ;
    public final boolean firstSide, endSide;
    protected final double dx, dy, dz, lengthSq, offX, offY, offZ, maxSize;
    protected double length;

    public Box(double startX, double startY, double startZ, double endX, double endY, double endZ,
               double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.firstSide = firstSide;
        this.endSide = endSide;
        dx = endX - startX; dy = endY - startY; dz = endZ - startZ;
        lengthSq = dx * dx + dy * dy + dz * dz;
        length = Math.sqrt(lengthSq);
        offX = sizeX / 2; offY = sizeY / 2; offZ = sizeZ / 2;
        maxSize = Math.max(Math.max(sizeX, sizeY), sizeZ);
    }

    public final double startX() { return startX; }
    public final double startY() { return startY; }
    public final double startZ() { return startZ; }
    public final double endX() { return endX; }
    public final double endY() { return endY; }
    public final double endZ() { return endZ; }
    public final double sizeX() { return sizeX; }
    public final double sizeY() { return sizeY; }
    public final double sizeZ() { return sizeZ; }
    public final boolean firstSide() { return firstSide; }
    public final boolean endSide() { return endSide; }

    public void render(BufferBuilder buffer, TextureAtlasSprite sprite) {
    double n1X = dx;
    double n1Y = Box.normalY(dx, dy, dz);
    double n1Z = dz;
    double n1Size = Math.sqrt(n1X * n1X + n1Y * n1Y + n1Z * n1Z);
    double n2X = dy * n1Z - dz * n1Y;
    double n2Z = dx * n1Y - dy * n1X;
    double n2Size = Math.sqrt(n2X * n2X + n2Z * n2Z);
    renderInternal(buffer, sprite, n1X / n1Size / 2, n1Y / n1Size / 2, n1Z / n1Size / 2, n2X / n2Size / 2, n2Z / n2Size / 2);
  
    }

    protected final void renderInternal(BufferBuilder b, TextureAtlasSprite sprite,
            double n1X, double n1Y, double n1Z, double n2X, double n2Z) {
    double eX = dx / length * sizeX;
    double eY = dy / length * sizeY;
    double eZ = dz / length * sizeZ;

    double e1X = startX + n1X * sizeX + n2X * sizeX;
    double e1Y = startY + n1Y * sizeY;
    double e1Z = startZ + n1Z * sizeZ + n2Z * sizeZ;

    double e2X = startX - n1X * sizeX + n2X * sizeX;
    double e2Y = startY - n1Y * sizeY;
    double e2Z = startZ - n1Z * sizeZ + n2Z * sizeZ;

    double e3X = startX - n1X * sizeX - n2X * sizeX;
    double e3Y = e2Y;
    double e3Z = startZ - n1Z * sizeZ - n2Z * sizeZ;

    double e4X = startX + n1X * sizeX - n2X * sizeX;
    double e4Y = e1Y;
    double e4Z = startZ + n1Z * sizeZ - n2Z * sizeZ;

    Buffer buffer = new Buffer(b);

    if (firstSide) {
      buffer.pos(e1X, e1Y, e1Z).colored().tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e2X, e2Y, e2Z).colored().tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e3X, e3Y, e3Z).colored().tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
      buffer.pos(e4X, e4Y, e4Z).colored().tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
    }
    double l = Math.sqrt(dx / sizeX * dx / sizeX + dy / sizeY * dy / sizeY + dz / sizeZ * dz / sizeZ);
    int lengthFloor = MathHelper.floor(l);
    int i1 = 0;
    while (i1 <= lengthFloor) {
      double i2 = (i1 == lengthFloor) ? l : i1 + 1;
      buffer.pos(e1X + eX * i2, e1Y + eY * i2, e1Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e1X + eX * i1, e1Y + eY * i1, e1Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e2X + eX * i1, e2Y + eY * i1, e2Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
      buffer.pos(e2X + eX * i2, e2Y + eY * i2, e2Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();

      buffer.pos(e2X + eX * i2, e2Y + eY * i2, e2Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e2X + eX * i1, e2Y + eY * i1, e2Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e3X + eX * i1, e3Y + eY * i1, e3Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
      buffer.pos(e3X + eX * i2, e3Y + eY * i2, e3Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();

      buffer.pos(e3X + eX * i2, e3Y + eY * i2, e3Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e3X + eX * i1, e3Y + eY * i1, e3Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e4X + eX * i1, e4Y + eY * i1, e4Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
      buffer.pos(e4X + eX * i2, e4Y + eY * i2, e4Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();

      buffer.pos(e4X + eX * i2, e4Y + eY * i2, e4Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e4X + eX * i1, e4Y + eY * i1, e4Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e1X + eX * i1, e1Y + eY * i1, e1Z + eZ * i1).colored().tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
      buffer.pos(e1X + eX * i2, e1Y + eY * i2, e1Z + eZ * i2).colored().tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();

      i1 += 1;
    }
    if (endSide) {
      buffer.pos(e1X + dx, e1Y + dy, e1Z + dz).colored().tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e2X + dx, e2Y + dy, e2Z + dz).colored().tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
      buffer.pos(e3X + dx, e3Y + dy, e3Z + dz).colored().tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
      buffer.pos(e4X + dx, e4Y + dy, e4Z + dz).colored().tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
    }
  
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof Box o)) return false;
        return startX == o.startX && startY == o.startY && startZ == o.startZ
                && endX == o.endX && endY == o.endY && endZ == o.endZ
                && sizeX == o.sizeX && sizeY == o.sizeY && sizeZ == o.sizeZ
                && firstSide == o.firstSide && endSide == o.endSide;
    }

    @Override public int hashCode() {
        return (int) (startX + startY + startZ + endX + endY + endZ + sizeX + sizeY + sizeZ
                + (firstSide ? 1 : 0) + (endSide ? 1 : 0));
    }

    public static Box apply(AxisAlignedBB box, double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
        return apply(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    public static Box apply(double startX, double startY, double startZ, double endX, double endY, double endZ,
                            double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
        if (startY == endY) {
            if (startX == endX) return new BoxZ(startZ, endZ, endX, endY, sizeX, sizeY, sizeZ, firstSide, endSide);
            if (startZ == endZ) return new BoxX(startX, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
            return new BoxXZ(startX, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
        }
        if (startZ == endZ && startX == endX) return new BoxY(startY, endY, endX, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
        return new Box(startX, startY, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    protected static double normalY(double x, double y, double z) { return -(x * x + z * z) / y; }

    private static final class BoxX extends Box {
        BoxX(double startX, double endX, double y, double z, double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
            super(startX, y, z, endX, y, z, sizeX, sizeY, sizeZ, firstSide, endSide);
            length = dx;
        }

        @Override public void render(BufferBuilder b, TextureAtlasSprite sprite) {
            double y = startY, z = startZ;

    int count = MathHelper.floor(length / sizeX);
    float minU = sprite.getMinU();
    float minV = sprite.getMinV();
    float maXV = sprite.getInterpolatedV(sizeX / maxSize * 16);
    float maYU = sprite.getInterpolatedU(sizeY / maxSize * 16);
    float maZU = sprite.getInterpolatedU(sizeZ / maxSize * 16);
    float maZV = sprite.getInterpolatedV(sizeZ / maxSize * 16);

    Buffer buffer = new Buffer(b);
    if (firstSide) {
      //West
      buffer.pos(startX, y + offY, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(startX, y - offY, z - offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(startX, y - offY, z + offZ).colored().tex(maYU, maZV).lightedAndEnd();
      buffer.pos(startX, y + offY, z + offZ).colored().tex(minU, maZV).lightedAndEnd();
    }
    int i1 = 0;
    while (i1 <= count) {
      double i2 = (i1 == count) ? length / sizeX : i1 + 1d;
      //North
      buffer.pos(startX + sizeX * i2, y + offY, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i2, y - offY, z - offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i1, y - offY, z - offZ).colored().tex(maYU, maXV).lightedAndEnd();
      buffer.pos(startX + sizeX * i1, y + offY, z - offZ).colored().tex(minU, maXV).lightedAndEnd();
      //South
      buffer.pos(startX + sizeX * i1, y + offY, z + offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i1, y - offY, z + offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i2, y - offY, z + offZ).colored().tex(maYU, maXV).lightedAndEnd();
      buffer.pos(startX + sizeX * i2, y + offY, z + offZ).colored().tex(minU, maXV).lightedAndEnd();
      //Top
      buffer.pos(startX + sizeX * i1, y + offY, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i1, y + offY, z + offZ).colored().tex(maZU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i2, y + offY, z + offZ).colored().tex(maZU, maXV).lightedAndEnd();
      buffer.pos(startX + sizeX * i2, y + offY, z - offZ).colored().tex(minU, maXV).lightedAndEnd();
      //Bottom
      buffer.pos(startX + sizeX * i2, y - offY, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i2, y - offY, z + offZ).colored().tex(maZU, minV).lightedAndEnd();
      buffer.pos(startX + sizeX * i1, y - offY, z + offZ).colored().tex(maZU, maXV).lightedAndEnd();
      buffer.pos(startX + sizeX * i1, y - offY, z - offZ).colored().tex(minU, maXV).lightedAndEnd();

      i1 += 1;
    }
    if (endSide) {
      //East
      buffer.pos(endX, y + offY, z + offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(endX, y - offY, z + offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(endX, y - offY, z - offZ).colored().tex(maYU, maZV).lightedAndEnd();
      buffer.pos(endX, y + offY, z - offZ).colored().tex(minU, maZV).lightedAndEnd();
    }
  
        }
    }

    private static final class BoxY extends Box {
        BoxY(double startY, double endY, double x, double z, double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
            super(x, startY, z, x, endY, z, sizeX, sizeY, sizeZ, firstSide, endSide);
            length = dy;
        }

        @Override public void render(BufferBuilder b, TextureAtlasSprite sprite) {
            double x = startX, z = startZ;

    int count = MathHelper.floor(length / sizeY);
    float minU = sprite.getMinU();
    float minV = sprite.getMinV();

    float maYU = sprite.getInterpolatedU(sizeY / maxSize * 16);
    float maXV = sprite.getInterpolatedV(sizeX / maxSize * 16);
    float maZU = sprite.getInterpolatedU(sizeZ / maxSize * 16);
    float maZV = sprite.getInterpolatedV(sizeZ / maxSize * 16);

    Buffer buffer = new Buffer(b);
    if (firstSide) {
      //Bottom
      buffer.pos(x + offX, startY, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x + offX, startY, z + offZ).colored().tex(maZU, minV).lightedAndEnd();
      buffer.pos(x - offX, startY, z + offZ).colored().tex(maZU, maXV).lightedAndEnd();
      buffer.pos(x - offX, startY, z - offZ).colored().tex(minU, maXV).lightedAndEnd();
    }
    int i1 = 0;
    while (i1 <= count) {
      double i2 = (i1 == count) ? length / sizeY : i1 + 1d;
      //West
      buffer.pos(x - offX, startY + sizeY * i2, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x - offX, startY + sizeY * i1, z - offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x - offX, startY + sizeY * i1, z + offZ).colored().tex(maYU, maZV).lightedAndEnd();
      buffer.pos(x - offX, startY + sizeY * i2, z + offZ).colored().tex(minU, maZV).lightedAndEnd();
      //East
      buffer.pos(x + offX, startY + sizeY * i2, z + offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x + offX, startY + sizeY * i1, z + offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x + offX, startY + sizeY * i1, z - offZ).colored().tex(maYU, maZV).lightedAndEnd();
      buffer.pos(x + offX, startY + sizeY * i2, z - offZ).colored().tex(minU, maZV).lightedAndEnd();
      //North
      buffer.pos(x + offX, startY + sizeY * i2, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x + offX, startY + sizeY * i1, z - offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x - offX, startY + sizeY * i1, z - offZ).colored().tex(maYU, maXV).lightedAndEnd();
      buffer.pos(x - offX, startY + sizeY * i2, z - offZ).colored().tex(minU, maXV).lightedAndEnd();
      //South
      buffer.pos(x - offX, startY + sizeY * i2, z + offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x - offX, startY + sizeY * i1, z + offZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x + offX, startY + sizeY * i1, z + offZ).colored().tex(maYU, maXV).lightedAndEnd();
      buffer.pos(x + offX, startY + sizeY * i2, z + offZ).colored().tex(minU, maXV).lightedAndEnd();

      i1 += 1;
    }
    if (endSide) {
      //Top
      buffer.pos(x - offX, endY, z - offZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x - offX, endY, z + offZ).colored().tex(maZU, minV).lightedAndEnd();
      buffer.pos(x + offX, endY, z + offZ).colored().tex(maZU, maXV).lightedAndEnd();
      buffer.pos(x + offX, endY, z - offZ).colored().tex(minU, maXV).lightedAndEnd();
    }
  
        }
    }

    private static final class BoxZ extends Box {
        BoxZ(double startZ, double endZ, double x, double y, double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
            super(x, y, startZ, x, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
            length = dz;
        }

        @Override public void render(BufferBuilder b, TextureAtlasSprite sprite) {
            double x = startX, y = startY;

    int count = MathHelper.floor(length / sizeZ);
    float minU = sprite.getMinU();
    float minV = sprite.getMinV();
    float maXU = sprite.getInterpolatedU(sizeX / maxSize * 16);
    float maXV = sprite.getInterpolatedV(sizeX / maxSize * 16);
    float maYU = sprite.getInterpolatedU(sizeY / maxSize * 16);
    float maZV = sprite.getInterpolatedV(sizeZ / maxSize * 16);

    Buffer buffer = new Buffer(b);
    if (firstSide) {
      //North
      buffer.pos(x + offX, y + offY, startZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x + offX, y - offY, startZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x - offX, y - offY, startZ).colored().tex(maYU, maXV).lightedAndEnd();
      buffer.pos(x - offX, y + offY, startZ).colored().tex(minU, maXV).lightedAndEnd();
    }
    int i1 = 0;
    while (i1 <= count) {
      double i2 = (i1 == count) ? length / sizeZ : i1 + 1d;
      //West
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i1).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i1).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i2).colored().tex(maYU, maZV).lightedAndEnd();
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i2).colored().tex(minU, maZV).lightedAndEnd();
      //East
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i2).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i2).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i1).colored().tex(maYU, maZV).lightedAndEnd();
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i1).colored().tex(minU, maZV).lightedAndEnd();
      //Top
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i1).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i2).colored().tex(maXU, minV).lightedAndEnd();
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i2).colored().tex(maXU, maZV).lightedAndEnd();
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i1).colored().tex(minU, maZV).lightedAndEnd();
      //Bottom
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i1).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i2).colored().tex(maXU, minV).lightedAndEnd();
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i2).colored().tex(maXU, maZV).lightedAndEnd();
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i1).colored().tex(minU, maZV).lightedAndEnd();

      i1 += 1;
    }
    if (endSide) {
      //South
      buffer.pos(x - offX, y + offY, endZ).colored().tex(minU, minV).lightedAndEnd();
      buffer.pos(x - offX, y - offY, endZ).colored().tex(maYU, minV).lightedAndEnd();
      buffer.pos(x + offX, y - offY, endZ).colored().tex(maYU, maXV).lightedAndEnd();
      buffer.pos(x + offX, y + offY, endZ).colored().tex(minU, maXV).lightedAndEnd();
    }
  
        }
    }

    private static final class BoxXZ extends Box {
        BoxXZ(double startX, double startZ, double endX, double y, double endZ, double sizeX, double sizeY, double sizeZ, boolean firstSide, boolean endSide) {
            super(startX, y, startZ, endX, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
            length = Math.sqrt(dx * dx + dz * dz);
        }

        @Override public void render(BufferBuilder buffer, TextureAtlasSprite sprite) {
            

    double n2Size = length;
    renderInternal(buffer, sprite, 0, 0.5, 0, -dz / n2Size / 2, dx / n2Size / 2);
  
        }
    }
}

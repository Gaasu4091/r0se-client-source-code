/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.font.TextRenderer$TextLayerType
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 */
package r0se.manager.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.render.RenderWorldEvent;
import r0se.api.render.BoxRenderMode;
import r0se.api.render.ColorUtil;
import r0se.impl.imixin.IWorldRendererAccess;
import r0se.manager.Manager;

public class RenderManager
implements Manager {
    private final List<QueuedBox> boxQueue = new ArrayList<QueuedBox>();
    private final List<QueuedLine> lineQueue = new ArrayList<QueuedLine>();
    private final List<QueuedWorldText> textQueue = new ArrayList<QueuedWorldText>();

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.boxQueue.clear();
        this.lineQueue.clear();
        this.textQueue.clear();
    }

    public void rect(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    public void horizontalGradient(DrawContext context, int x, int y, int width, int height, int leftColor, int rightColor) {
        for (int i = 0; i < width; ++i) {
            float delta = width <= 1 ? 0.0f : (float)i / (float)(width - 1);
            context.fill(x + i, y, x + i + 1, y + height, ColorUtil.lerp(leftColor, rightColor, delta));
        }
    }

    public void verticalGradient(DrawContext context, int x, int y, int width, int height, int topColor, int bottomColor) {
        for (int i = 0; i < height; ++i) {
            float delta = height <= 1 ? 0.0f : (float)i / (float)(height - 1);
            context.fill(x, y + i, x + width, y + i + 1, ColorUtil.lerp(topColor, bottomColor, delta));
        }
    }

    public void outline(DrawContext context, int x, int y, int width, int height, int color) {
        this.rect(context, x, y, x + width, y + 1, color);
        this.rect(context, x, y + height - 1, x + width, y + height, color);
        this.rect(context, x, y, x + 1, y + height, color);
        this.rect(context, x + width - 1, y, x + width, y + height, color);
    }

    public void text(DrawContext context, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
        context.drawText(renderer, text, x, y, color, shadow);
    }

    public void centeredText(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
        int width = renderer.getWidth(text);
        context.drawText(renderer, text, centerX - width / 2, y, color, shadow);
    }

    public int textWidth(TextRenderer renderer, String text) {
        return renderer.getWidth(text);
    }

    public int textHeight(TextRenderer renderer) {
        Objects.requireNonNull(renderer);
        return 9;
    }

    public void enableScissor(DrawContext context, int x1, int y1, int x2, int y2) {
        context.enableScissor(x1, y1, x2, y2);
    }

    public void disableScissor(DrawContext context) {
        context.disableScissor();
    }

    public void rawScissor(int x, int y, int width, int height) {
        double scale = R0SE.mc.getWindow().getScaleFactor();
        int framebufferHeight = R0SE.mc.getWindow().getFramebufferHeight();
        int scissorX = (int)((double)x * scale);
        int scissorY = (int)((double)framebufferHeight - (double)(y + height) * scale);
        int scissorWidth = (int)((double)width * scale);
        int scissorHeight = (int)((double)height * scale);
        RenderSystem.enableScissor((int)scissorX, (int)scissorY, (int)scissorWidth, (int)scissorHeight);
    }

    public void disableRawScissor() {
        RenderSystem.disableScissor();
    }

    public boolean isVisible(Box box) {
        if (R0SE.mc.worldRenderer == null) {
            return true;
        }
        return ((IWorldRendererAccess)R0SE.mc.worldRenderer).r0se$getFrustum().isVisible(box);
    }

    public void renderBox(MatrixStack matrices, BlockPos pos, Color color, RenderLayer layer, float lineWidth) {
        this.renderBox(matrices, new Box(pos), color, layer, lineWidth);
    }

    public void renderBox(MatrixStack matrices, Box box, Color color, RenderLayer layer, float lineWidth) {
        this.renderBox(matrices, box, color, true, true, layer, lineWidth);
    }

    public void renderBox(MatrixStack matrices, BlockPos pos, BoxRenderMode mode, Color color, float lineWidth) {
        this.renderBox(matrices, new Box(pos), mode, color, RenderLayer.getLines(), lineWidth);
    }

    public void renderBox(MatrixStack matrices, Box box, BoxRenderMode mode, Color color, float lineWidth) {
        this.renderBox(matrices, box, mode, color, RenderLayer.getLines(), lineWidth);
    }

    public void renderBox(MatrixStack matrices, Box box, BoxRenderMode mode, Color color, RenderLayer layer, float lineWidth) {
        this.renderBox(matrices, box, color, mode.isFill(), mode.isOutline(), layer, lineWidth);
    }

    public void renderBox(MatrixStack matrices, BlockPos pos, Color color, boolean fill, boolean outline, RenderLayer layer, float lineWidth) {
        this.renderBox(matrices, new Box(pos), color, fill, outline, layer, lineWidth);
    }

    public void renderBox(MatrixStack matrices, Box box, Color color, boolean fill, boolean outline, RenderLayer layer, float lineWidth) {
        if (R0SE.mc.gameRenderer == null || R0SE.mc.gameRenderer.getCamera() == null || R0SE.mc.world == null || !this.isVisible(box)) {
            return;
        }
        this.boxQueue.add(new QueuedBox(box, color, fill, outline, lineWidth));
    }

    public void renderScaledBox(MatrixStack matrices, BlockPos pos, float scale, Color color, boolean fill, boolean outline, RenderLayer layer, float lineWidth) {
        this.renderScaledBox(matrices, new Box(pos), scale, color, fill, outline, layer, lineWidth);
    }

    public void renderScaledBox(MatrixStack matrices, BlockPos pos, float scale, BoxRenderMode mode, Color color, float lineWidth) {
        this.renderScaledBox(matrices, new Box(pos), scale, mode, color, RenderLayer.getLines(), lineWidth);
    }

    public void renderScaledBox(MatrixStack matrices, Box box, float scale, BoxRenderMode mode, Color color, float lineWidth) {
        this.renderScaledBox(matrices, box, scale, mode, color, RenderLayer.getLines(), lineWidth);
    }

    public void renderScaledBox(MatrixStack matrices, Box box, float scale, BoxRenderMode mode, Color color, RenderLayer layer, float lineWidth) {
        this.renderScaledBox(matrices, box, scale, color, mode.isFill(), mode.isOutline(), layer, lineWidth);
    }

    public void renderScaledBox(MatrixStack matrices, Box box, float scale, Color color, boolean fill, boolean outline, RenderLayer layer, float lineWidth) {
        double clampedScale = Math.max(0.05, (double)scale);
        Vec3d center = box.getCenter();
        double halfX = box.getLengthX() * 0.5 * clampedScale;
        double halfY = box.getLengthY() * 0.5 * clampedScale;
        double halfZ = box.getLengthZ() * 0.5 * clampedScale;
        Box scaled = new Box(center.x - halfX, center.y - halfY, center.z - halfZ, center.x + halfX, center.y + halfY, center.z + halfZ);
        this.renderBox(matrices, scaled, color, fill, outline, layer, lineWidth);
    }

    public void renderLine(Vec3d start, Vec3d end, Color color, float lineWidth) {
        Box bounds = new Box(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z), Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z));
        if (!this.isVisible(bounds)) {
            return;
        }
        this.lineQueue.add(new QueuedLine(start, end, color, lineWidth));
    }

    public void renderWorldText(Vec3d pos, String text, float scale, int color, boolean shadow) {
        this.renderWorldText(null, pos, text, scale, color, shadow);
    }

    public void renderWorldText(MatrixStack matrices, Vec3d pos, String text, float scale, int color, boolean shadow) {
        if (!this.isVisible(Box.of((Vec3d)pos, (double)0.25, (double)0.25, (double)0.25))) {
            return;
        }
        this.textQueue.add(new QueuedWorldText(pos, text, scale, color, shadow));
    }

    @Subscribe(priority=-2147483648)
    public void onRenderWorld(RenderWorldEvent event) {
        this.flushBoxQueue(event.getMatrices());
        this.flushLineQueue(event.getMatrices());
        this.flushTextQueue();
    }

    private void flushBoxQueue(MatrixStack matrices) {
        if (this.boxQueue.isEmpty() || R0SE.mc.gameRenderer == null || R0SE.mc.gameRenderer.getCamera() == null) {
            return;
        }
        this.startRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Vec3d camera = R0SE.mc.gameRenderer.getCamera().getPos();
        boolean hasFill = this.boxQueue.stream().anyMatch(QueuedBox::fill);
        boolean hasOutline = this.boxQueue.stream().anyMatch(QueuedBox::outline);
        if (hasFill) {
            BufferBuilder fillBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (QueuedBox queued : this.boxQueue) {
                if (!queued.fill) continue;
                this.writeFilledBox(fillBuffer, matrices.peek().getPositionMatrix(), queued.box.offset(-camera.x, -camera.y, -camera.z), queued.color);
            }
            this.drawBuiltBuffer(fillBuffer.endNullable());
        }
        if (hasOutline) {
            BufferBuilder lineBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (QueuedBox queued : this.boxQueue) {
                if (!queued.outline) continue;
                GL11.glLineWidth((float)queued.lineWidth);
                this.writeBoundingBox(lineBuffer, matrices.peek().getPositionMatrix(), queued.box.offset(-camera.x, -camera.y, -camera.z), queued.color);
            }
            this.drawBuiltBuffer(lineBuffer.endNullable());
        }
        this.endRender();
        this.boxQueue.clear();
    }

    private void flushLineQueue(MatrixStack matrices) {
        if (this.lineQueue.isEmpty() || R0SE.mc.gameRenderer == null || R0SE.mc.gameRenderer.getCamera() == null) {
            return;
        }
        this.startRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Vec3d camera = R0SE.mc.gameRenderer.getCamera().getPos();
        BufferBuilder lineBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (QueuedLine queued : this.lineQueue) {
            GL11.glLineWidth((float)queued.lineWidth);
            this.writeLine(lineBuffer, matrices.peek().getPositionMatrix(), queued.start.subtract(camera), queued.end.subtract(camera), queued.color);
        }
        this.drawBuiltBuffer(lineBuffer.endNullable());
        this.endRender();
        this.lineQueue.clear();
    }

    private void flushTextQueue() {
        if (this.textQueue.isEmpty() || R0SE.mc.gameRenderer == null || R0SE.mc.gameRenderer.getCamera() == null) {
            return;
        }
        Vec3d camera = R0SE.mc.gameRenderer.getCamera().getPos();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (QueuedWorldText queued : this.textQueue) {
            MatrixStack matrices = new MatrixStack();
            Vec3d relative = queued.pos.subtract(camera);
            matrices.push();
            matrices.translate(relative.x, relative.y, relative.z);
            matrices.multiply(R0SE.mc.getEntityRenderDispatcher().getRotation());
            float distance = (float)Math.sqrt(camera.squaredDistanceTo(queued.pos));
            float worldScale = distance <= 8.0f ? 0.0245f : 0.0018f + queued.scale * distance;
            matrices.scale(-worldScale, -worldScale, worldScale);
            TextRenderer textRenderer = R0SE.mc.textRenderer;
            int width = textRenderer.getWidth(queued.text);
            VertexConsumerProvider.Immediate consumers = R0SE.mc.getBufferBuilders().getEntityVertexConsumers();
            textRenderer.draw(queued.text, (float)(-width) / 2.0f, 0.0f, queued.color, queued.shadow, matrices.peek().getPositionMatrix(), (VertexConsumerProvider)consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            consumers.draw();
            matrices.pop();
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        this.textQueue.clear();
    }

    private void writeFilledBox(BufferBuilder buffer, Matrix4f matrix, Box box, Color color) {
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        int argb = ColorUtil.rgba(color.getRed(), color.getGreen(), color.getBlue(), Math.min(color.getAlpha(), 90));
        buffer.vertex(matrix, minX, minY, minZ).color(argb);
        buffer.vertex(matrix, maxX, minY, minZ).color(argb);
        buffer.vertex(matrix, maxX, minY, maxZ).color(argb);
        buffer.vertex(matrix, minX, minY, maxZ).color(argb);
        buffer.vertex(matrix, minX, maxY, minZ).color(argb);
        buffer.vertex(matrix, minX, maxY, maxZ).color(argb);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(argb);
        buffer.vertex(matrix, maxX, maxY, minZ).color(argb);
        buffer.vertex(matrix, minX, minY, minZ).color(argb);
        buffer.vertex(matrix, minX, maxY, minZ).color(argb);
        buffer.vertex(matrix, maxX, maxY, minZ).color(argb);
        buffer.vertex(matrix, maxX, minY, minZ).color(argb);
        buffer.vertex(matrix, maxX, minY, minZ).color(argb);
        buffer.vertex(matrix, maxX, maxY, minZ).color(argb);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(argb);
        buffer.vertex(matrix, maxX, minY, maxZ).color(argb);
        buffer.vertex(matrix, minX, minY, maxZ).color(argb);
        buffer.vertex(matrix, maxX, minY, maxZ).color(argb);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(argb);
        buffer.vertex(matrix, minX, maxY, maxZ).color(argb);
        buffer.vertex(matrix, minX, minY, minZ).color(argb);
        buffer.vertex(matrix, minX, minY, maxZ).color(argb);
        buffer.vertex(matrix, minX, maxY, maxZ).color(argb);
        buffer.vertex(matrix, minX, maxY, minZ).color(argb);
    }

    private void writeBoundingBox(BufferBuilder buffer, Matrix4f matrix, Box box, Color color) {
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        int argb = ColorUtil.rgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        this.lineVertex(buffer, matrix, minX, minY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, minY, maxZ, argb);
        this.lineVertex(buffer, matrix, minX, minY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, minY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, minY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, minY, minZ, argb);
        this.lineVertex(buffer, matrix, maxX, minY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, minY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, maxY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, maxY, maxZ, argb);
        this.lineVertex(buffer, matrix, minX, maxY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, maxY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, maxY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, maxY, minZ, argb);
        this.lineVertex(buffer, matrix, maxX, maxY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, maxY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, minY, minZ, argb);
        this.lineVertex(buffer, matrix, minX, maxY, minZ, argb);
        this.lineVertex(buffer, matrix, maxX, minY, minZ, argb);
        this.lineVertex(buffer, matrix, maxX, maxY, minZ, argb);
        this.lineVertex(buffer, matrix, maxX, minY, maxZ, argb);
        this.lineVertex(buffer, matrix, maxX, maxY, maxZ, argb);
        this.lineVertex(buffer, matrix, minX, minY, maxZ, argb);
        this.lineVertex(buffer, matrix, minX, maxY, maxZ, argb);
    }

    private void writeLine(BufferBuilder buffer, Matrix4f matrix, Vec3d start, Vec3d end, Color color) {
        int argb = ColorUtil.rgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        this.lineVertex(buffer, matrix, (float)start.x, (float)start.y, (float)start.z, argb);
        this.lineVertex(buffer, matrix, (float)end.x, (float)end.y, (float)end.z, argb);
    }

    private void lineVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int argb) {
        buffer.vertex(matrix, x, y, z).color(argb);
    }

    private void drawBuiltBuffer(BuiltBuffer builtBuffer) {
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
    }

    private void startRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.disableCull();
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glEnable((int)2881);
        GL11.glHint((int)3155, (int)4354);
    }

    private void endRender() {
        GL11.glDisable((int)2881);
        GL11.glDisable((int)2848);
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private record QueuedBox(Box box, Color color, boolean fill, boolean outline, float lineWidth) {
    }

    private record QueuedLine(Vec3d start, Vec3d end, Color color, float lineWidth) {
    }

    private record QueuedWorldText(Vec3d pos, String text, float scale, int color, boolean shadow) {
    }
}




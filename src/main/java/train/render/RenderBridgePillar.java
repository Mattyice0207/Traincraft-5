package train.render;

import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import train.render.models.blocks.ModelBridgePillar;

public class RenderBridgePillar extends TiMTESR {

	static final ModelBridgePillar modelBridgePillar = new ModelBridgePillar();

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		org.lwjgl.opengl.GL11.glTranslatef(0.5f,0.3f,0.5f);
		modelBridgePillar.render(tileEntity, x, y, z);
		GL11.glPopMatrix();
	}
}

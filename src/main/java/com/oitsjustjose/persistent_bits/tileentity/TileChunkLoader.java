package com.oitsjustjose.persistent_bits.tileentity;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.oitsjustjose.persistent_bits.PersistentBits;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class TileChunkLoader extends TileEntity
{
	private Ticket chunkTicket;
	private GameProfile owner;
	private boolean showingChunks;

	public boolean isShowingChunks()
	{
		return this.showingChunks;
	}

	public void setChunksShown()
	{
		this.showingChunks = true;
	}

	public void setChunksHidden()
	{
		this.showingChunks = false;
	}

	public GameProfile getOwner()
	{
		return this.owner;
	}

	public void setOwner(GameProfile profile)
	{
		this.owner = profile;
	}

	public List<ChunkPos> getLoadArea()
	{
		List<ChunkPos> loadArea = new LinkedList<ChunkPos>();
		int radMax = PersistentBits.config.radius;
		int radMin = 0 - radMax;

		// pull chunk coord transform out of the loop
		int cx = this.getPos().getX() >> 4;
		int cz = this.getPos().getZ() >> 4;

		// previous loop went from -R to (R-1), so one chunk further in
		// both negative directions. this change goes from -R to R instead.
		//
		// or you can do "xMod = radMin + 1; xMod < radMax" if -(R-1) to (R-1)
		// was the intention... depends on whether you want to define
		// radius to include the center chunk
		for (int xMod = radMin; xMod <= radMax; xMod++)
		{
			for (int zMod = radMin; zMod <= radMax; zMod++)
			{
				// simplified offset math
				int chunkXNew = cx + xMod;
				int chunkZNew = cz + zMod;

				loadArea.add(new ChunkPos(chunkXNew, chunkZNew));
			}
		}
		return loadArea;
	}

	@Override
	public void validate()
	{
		super.validate();
		if ((!this.worldObj.isRemote) && (this.chunkTicket == null))
		{
			Ticket ticket = ForgeChunkManager.requestTicket(PersistentBits.INSTANCE, this.worldObj, ForgeChunkManager.Type.NORMAL);
			if (ticket != null)
			{
				forceChunkLoading(ticket);
			}
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		stopChunkLoading();
	}

	public void forceChunkLoading(Ticket ticket)
	{
		this.chunkTicket = ticket;

		if (ticket != null)
		{
			ticket.getModData().setInteger("x", pos.getX());
			ticket.getModData().setInteger("y", pos.getY());
			ticket.getModData().setInteger("z", pos.getZ());
			
			for (ChunkPos coord : getLoadArea())
			{
				ForgeChunkManager.forceChunk(ticket, coord);
			}
		}
	}

	public void stopChunkLoading()
	{
		if (this.chunkTicket != null)
		{
			ForgeChunkManager.releaseTicket(this.chunkTicket);
			this.chunkTicket = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		String owner = compound.getString("ownerName");
		UUID id = compound.getUniqueId("uuid");

		this.owner = new GameProfile(id, owner);

		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setString("ownerName", this.owner.getName());
		compound.setUniqueId("uuid", this.owner.getId());

		return super.writeToNBT(compound);
	}
}

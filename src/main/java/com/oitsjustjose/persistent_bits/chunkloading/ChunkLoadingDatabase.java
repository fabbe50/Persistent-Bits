package com.oitsjustjose.persistent_bits.chunkloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import com.oitsjustjose.persistent_bits.PersistentBits;

import net.minecraftforge.common.DimensionManager;

/**
 * @author oitsjustjose
 * 
 *         A class for storing and serializing the locations of all chunk loaders on server start
 */

public class ChunkLoadingDatabase
{
	private HashSet<DetailedCoordinate> chunkLoaderCoords = new HashSet<DetailedCoordinate>();
	File fileLocation = new File(DimensionManager.getCurrentSaveRootDirectory(), "PersistentBits.dat");

	public void addChunkCoord(DetailedCoordinate newCoord)
	{
		this.chunkLoaderCoords.add(newCoord);
		this.serialize();
	}

	public void removeChunkCoord(DetailedCoordinate coordToRemove)
	{
		for (DetailedCoordinate d : this.chunkLoaderCoords)
			if (d.equals(coordToRemove))
				this.chunkLoaderCoords.remove(d);
		this.serialize();
	}

	public HashSet<DetailedCoordinate> getCoordinates()
	{
		return this.chunkLoaderCoords;
	}

	public void serialize()
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream(fileLocation);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.chunkLoaderCoords);
			out.close();
			fileOut.close();
		}
		catch (IOException i)
		{
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void deserialize()
	{

		try
		{
			if (fileLocation.exists())
			{

				FileInputStream fileIn = new FileInputStream(fileLocation);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				this.chunkLoaderCoords = (HashSet<DetailedCoordinate>) in.readObject();
				in.close();
				fileIn.close();
			}
		}
		catch (IOException i)
		{
			PersistentBits.LOGGER.info("DERP IOEx");
			return;
		}
		catch (ClassNotFoundException c)
		{
			PersistentBits.LOGGER.info("DERP ClassNotFoundEx");
			return;
		}
	}

}
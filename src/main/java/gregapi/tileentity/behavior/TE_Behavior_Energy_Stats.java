/**
 * Copyright (c) 2021 GregTech-6 Team
 *
 * This file is part of GregTech.
 *
 * GregTech is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GregTech is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GregTech. If not, see <http://www.gnu.org/licenses/>.
 */

package gregapi.tileentity.behavior;

import cn.kuzuanpa.ktfruaddon.api.tile.IMeterDetectable;
import gregapi.code.TagData;
import gregapi.data.LH;
import gregapi.data.LH.Chat;
import gregapi.util.UT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static gregapi.data.CS.F;
import static gregapi.data.CS.T;

public class TE_Behavior_Energy_Stats extends TE_Behavior_Energy implements IMeterDetectable{
	public long mRec, mMin, mMax, mAmount = 1;
	public boolean mOverloaded = F;
	public TE_Behavior_Energy_Capacitor mStorage;
	public ArrayList<IMeterDetectable.MeterData> receivedEnergy =new ArrayList<>(),receivedEnergyLast  = new ArrayList<>();

	public TE_Behavior_Energy_Stats(TileEntity aTileEntity, NBTTagCompound aNBT, TagData aEnergyType, TE_Behavior_Energy_Capacitor aStorage, long aSizeMin, long aSizeRec, long aSizeMax) {
		super(aTileEntity, aNBT, aEnergyType);
		mStorage = aStorage; mMin = Math.abs(aSizeMin); mRec = Math.abs(aSizeRec); mMax = Math.abs(aSizeMax);
	}

	public TE_Behavior_Energy_Stats setAmount(long mAmount) {
		this.mAmount = mAmount;
		return this;
	}

	public boolean isType(TagData aEnergyType) {return mType == aEnergyType;}
	public long sizeMin(TagData aEnergyType) {return mType != aEnergyType ? 0 : mMin;}
	public long sizeRec(TagData aEnergyType) {return mType != aEnergyType ? 0 : mRec;}
	public long sizeMax(TagData aEnergyType) {return mType != aEnergyType ? 0 : mMax;}
	public Collection<TagData> getTypes() {return mType.AS_LIST;}
	
	public void addToolTips(List<String> aList, ItemStack aStack, boolean aF3_H, String aSides, boolean aEmitting) {
		aList.add((aEmitting ? (Chat.RED + LH.get(LH.ENERGY_OUTPUT)) : (Chat.GREEN + LH.get(LH.ENERGY_INPUT))) + ": " + Chat.WHITE +mMin+" - "+mMax+" "+mType.getLocalisedChatNameShort()+Chat.WHITE+(mAmount==1?"/t ":"/A * "+Chat.CYAN+mAmount+" A/t ")+Chat.WHITE+(UT.Code.stringInvalid(aSides)?"":", "+aSides));
		aList.add(aEmitting ? LH.getToolTipRedstoneFluxEmit(mType) : LH.getToolTipRedstoneFluxAccept(mType));
	}

	public void onTick(){
		receivedEnergyLast = receivedEnergy;
		receivedEnergy = new ArrayList<>();
	}
	public long doInject(long aSize, long aAmount, boolean aDoInject) {
		aSize = Math.abs(aSize);
		if (aSize > mMax) {
			if (aDoInject) mOverloaded = T;
			receivedEnergy.add(new IMeterDetectable.MeterData(mType, aSize, aAmount));
			return aAmount;
		}
		if (mStorage == null || mStorage.mEnergy >= mStorage.mCapacity) return 0;
		long tInput = Math.min(mStorage.mCapacity - mStorage.mEnergy, aSize * aAmount), tConsumed = Math.min(aAmount, (tInput/aSize) + (tInput%aSize!=0?1:0));
		if (aDoInject) mStorage.mEnergy += tConsumed * aSize;
		receivedEnergy.add(new IMeterDetectable.MeterData(mType, aSize, tConsumed));
		return tConsumed;
	}
}

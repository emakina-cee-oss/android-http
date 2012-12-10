/*
 * Copyright (C) 2012 the diamond:dogs|group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.diamonddogs.data.adapter.parcelable;

import android.os.Parcel;
import android.os.Parcelable;
import at.diamonddogs.data.dataobjects.TempFile;

public class ParcelableAdapterTempFile extends ParcelableAdapter<TempFile> {

	public ParcelableAdapterTempFile(Parcel in) {
		super(in);
		dataObject.setUrl(in.readString());
		dataObject.setChecksum(in.readString());
		dataObject.setPath(in.readString());
		dataObject.setDate(in.readLong());
		dataObject.setSize(in.readInt());
		dataObject.setUseChecksum(in.readInt() == 0 ? false : true);
	}

	public ParcelableAdapterTempFile(TempFile dataObject) {
		super(dataObject);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(dataObject.getUrl());
		dest.writeString(dataObject.getChecksum());
		dest.writeString(dataObject.getPath());
		dest.writeLong(dataObject.getDate());
		dest.writeInt(dataObject.getSize());
		dest.writeInt(dataObject.isUseChecksum() ? 1 : 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ParcelableAdapterTempFile> CREATOR = new Parcelable.Creator<ParcelableAdapterTempFile>() {
		public ParcelableAdapterTempFile createFromParcel(Parcel in) {
			return new ParcelableAdapterTempFile(in);
		}

		public ParcelableAdapterTempFile[] newArray(int size) {
			return new ParcelableAdapterTempFile[size];
		}
	};
}

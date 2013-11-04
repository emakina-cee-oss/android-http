/*
 * Copyright (C) 2012, 2013 the diamond:dogs|group
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
import at.diamonddogs.data.dataobjects.WebReply;

/**
 * Use this {@link ParcelableAdapter} to parcel {@link WebReply}s
 * 
 * TODO: check if all data is parcelled!
 */
public class ParcelableAdapterWebReply extends ParcelableAdapter<WebReply> {
	/**
	 * Required by Parcelable mechanism
	 * 
	 * @param in
	 *            the input parcel
	 */
	public ParcelableAdapterWebReply(Parcel in) {
		super(in);
		if (dataObject == null) {
			dataObject = new WebReply();
		}

		dataObject.setHttpStatusCode(in.readInt());
		byte[] data = new byte[in.readInt()];
		in.readByteArray(data);
		dataObject.setData(data);
		dataObject.setReplyHeader(readHeaderMap(in, dataObject.getReplyHeader()));
	}

	/**
	 * Constructs a {@link ParcelableAdapterWebReply} from a given input object
	 * 
	 * @param dataObject
	 *            the object that should be made parcelable
	 */
	public ParcelableAdapterWebReply(WebReply dataObject) {
		super(dataObject);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeInt(dataObject.getHttpStatusCode());
		dest.writeInt(dataObject.getData().length);
		dest.writeByteArray(dataObject.getData());
		writeHeaderMap(dest, dataObject.getReplyHeader());
	}

	/**
	 * Required by Parcelable mechanism
	 */
	public static final Parcelable.Creator<ParcelableAdapterWebReply> CREATOR = new Parcelable.Creator<ParcelableAdapterWebReply>() {
		@Override
		public ParcelableAdapterWebReply createFromParcel(Parcel in) {
			return new ParcelableAdapterWebReply(in);
		}

		@Override
		public ParcelableAdapterWebReply[] newArray(int size) {
			return new ParcelableAdapterWebReply[size];
		}
	};
}

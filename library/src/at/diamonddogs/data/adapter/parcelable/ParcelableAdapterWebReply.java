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
import at.diamonddogs.data.dataobjects.WebReply;

public class ParcelableAdapterWebReply extends ParcelableAdapter<WebReply> {

	public ParcelableAdapterWebReply(Parcel in) {
		super(in);
		dataObject.setHttpStatusCode(in.readInt());
		byte[] data = new byte[in.readInt()];
		in.readByteArray(data);
		dataObject.setData(data);
		dataObject.setReplyHeader(readHeaderMap(in, dataObject.getReplyHeader()));
	}

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

	public static final Parcelable.Creator<ParcelableAdapterWebReply> CREATOR = new Parcelable.Creator<ParcelableAdapterWebReply>() {
		public ParcelableAdapterWebReply createFromParcel(Parcel in) {
			return new ParcelableAdapterWebReply(in);
		}

		public ParcelableAdapterWebReply[] newArray(int size) {
			return new ParcelableAdapterWebReply[size];
		}
	};
}

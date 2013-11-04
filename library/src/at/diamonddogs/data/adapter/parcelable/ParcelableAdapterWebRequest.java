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

import java.net.URL;

import org.apache.http.HttpEntity;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import at.diamonddogs.data.dataobjects.TempFile;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;

/**
 * Use this {@link ParcelableAdapterWebRequest} to parcel {@link WebReply}s
 * 
 * TODO: check if all data is parcelled!
 */
public class ParcelableAdapterWebRequest extends ParcelableAdapter<WebRequest> {

	protected HttpEntity httpEntity;

	/**
	 * Required by Parcelable mechanism
	 * 
	 * @param in
	 *            the input parcel
	 */
	public ParcelableAdapterWebRequest(Parcel in) {
		super(in);
		if (dataObject == null) {
			dataObject = new WebRequest();
		}
		dataObject.setProcessorId(in.readInt());
		dataObject.setRequestType((Type) in.readSerializable());
		dataObject.setUrl((URL) in.readSerializable());
		dataObject.setReadTimeout(in.readInt());
		dataObject.setConnectionTimeout(in.readInt());
		dataObject.setFollowRedirects(in.readInt() == 1);

		boolean first = (in.readInt() == 1);
		if (first) {
			ParcelableAdapterTempFile tmp = in.readParcelable(ClassLoader.getSystemClassLoader());
			dataObject.setTmpFile(new Pair<Boolean, TempFile>(first, tmp.getDataObject()));
		}

		dataObject.setHeader(readStringMap(in));
		dataObject.setCacheTime(in.readLong());
		dataObject.setNumberOfRetries(in.readInt());
		dataObject.setRetryInterval(in.readInt());

		dataObject.setCancelled(in.readInt() == 1);
		dataObject.setCheckConnectivity(in.readInt() == 1);
		dataObject.setCheckConnectivityPing(in.readInt() == 1);
		dataObject.setUseOfflineCache(in.readInt() == 1);

	}

	/**
	 * Constructs a {@link ParcelableAdapterWebRequest} from a given input
	 * object
	 * 
	 * @param dataObject
	 *            the object that should be made parcelable
	 */
	public ParcelableAdapterWebRequest(WebRequest dataObject) {
		super(dataObject);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(dataObject.getProcessorId());
		dest.writeSerializable(dataObject.getRequestType());
		dest.writeSerializable(dataObject.getUrl());
		dest.writeInt(dataObject.getReadTimeout());
		dest.writeInt(dataObject.getConnectionTimeout());

		// Parcel.writeBoolean is missing m(
		// workaround: http://code.google.com/p/android/issues/detail?id=5973
		dest.writeInt(dataObject.isFollowRedirects() ? 1 : 0);
		dest.writeInt(dataObject.getTmpFile().first ? 1 : 0);
		if (dataObject.getTmpFile().first) {
			dest.writeParcelable(new ParcelableAdapterTempFile(dataObject.getTmpFile().second), 0);
		}

		writeStringMap(dest, dataObject.getHeader());
		dest.writeLong(dataObject.getCacheTime());
		dest.writeInt(dataObject.getNumberOfRetries());
		dest.writeInt(dataObject.getRetryInterval());

		dest.writeInt(dataObject.isCancelled() ? 1 : 0);
		dest.writeInt(dataObject.isCheckConnectivity() ? 1 : 0);
		dest.writeInt(dataObject.isCheckConnectivityPing() ? 1 : 0);
		dest.writeInt(dataObject.isUseOfflineCache() ? 1 : 0);
	}

	/**
	 * Required by Parcelable mechanism
	 */
	public static final Parcelable.Creator<ParcelableAdapterWebRequest> CREATOR = new Parcelable.Creator<ParcelableAdapterWebRequest>() {
		@Override
		public ParcelableAdapterWebRequest createFromParcel(Parcel in) {
			return new ParcelableAdapterWebRequest(in);
		}

		@Override
		public ParcelableAdapterWebRequest[] newArray(int size) {
			return new ParcelableAdapterWebRequest[size];
		}
	};
}

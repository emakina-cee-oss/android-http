/*
 * Copyright (C) 2013 the diamond:dogs|group
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
package at.diamonddogs.example.http.view.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.example.http.R;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.ImageProcessor;

/**
 * An Image {@link ArrayAdapter} that utilizes {@link HttpServiceAssister} to
 * load images onto {@link ImageView}s
 */
public class ImageLoadingExampleAdapter extends ArrayAdapter<String> {
	private HttpServiceAssister assister;

	public ImageLoadingExampleAdapter(Context context, HttpServiceAssister assister, List<String> data) {
		super(context, -1, data);
		this.assister = assister;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.imageloadingexampleadapter, parent, false);
			ViewHolder vh = new ViewHolder();
			vh.image = (ImageView) convertView.findViewById(R.id.imageloadingexampleadapter_image);
			convertView.setTag(vh);
		}
		ViewHolder vh = (ViewHolder) convertView.getTag();
		String item = getItem(position);

		WebRequest wr = ImageProcessor.getDefaultImageRequest(item);
		assister.runWebRequest(new ImageProcessor.ImageProcessHandler(vh.image, item), wr, new ImageProcessor());

		return convertView;
	}

	private static final class ViewHolder {
		private ImageView image;
	}
}

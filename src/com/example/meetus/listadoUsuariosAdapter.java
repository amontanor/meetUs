package com.example.meetus;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParsePush;


public class listadoUsuariosAdapter extends ArrayAdapter<Usuarios>{

	int resource;
	
	public listadoUsuariosAdapter(Context context, int resource,
			List<Usuarios> usuarios) {
		super(context, resource, usuarios);
		this.resource = resource;
	}
	
	@Override
	 public View getView(int position, View convertView, ViewGroup parent) {
	 LinearLayout todoView;
	 Usuarios usuario = getItem(position);

	 if (convertView == null) {
		 todoView = new LinearLayout(getContext());
		 String inflater = Context.LAYOUT_INFLATER_SERVICE;
		 LayoutInflater li;
		 li = (LayoutInflater)getContext().getSystemService(inflater);
		 li.inflate(resource, todoView, true);
	 } else {
	 todoView = (LinearLayout) convertView;
	 }

	 TextView nombre = (TextView)todoView.findViewById(R.id.nombre);
	 ImageView foto = (ImageView) todoView.findViewById(R.id.foto);
	 
	 nombre.setText(usuario.nombre);
	 foto.setImageBitmap(usuario.getFoto());
	 	 
	 return todoView;

	 }

}

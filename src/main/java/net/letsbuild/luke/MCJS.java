package net.letsbuild.luke;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class MCJS extends Plugin {

	public static Plugin plugin;

	private static ScriptEngine        jsEngine;
	private static ScriptEngineManager scriptManager;

	private static String pluginDir;
	private static String jsFilePath;

	public void reloadCommand () {

		super( "js");
		reload();
	}

	private String getPluginDir () {

		if ( pluginDir == null ) {
			pluginDir = Paths.get( Paths.get( System.getProperty( "user.dir" ), "plugins" ).toString(), "MCJS" ).toString();
		}

		return pluginDir;
	}

	private String getJSFile () {

		if ( jsFilePath == null ) {
			jsFilePath = Paths.get( getPluginDir(), "lib", "init.js" ).toString();
		}

		String javascript = "";

		File file   = new File( jsFilePath );
		byte[] data = new byte[ ( int ) file.length() ];

		try {
			FileInputStream fileStream = new FileInputStream( file );

			fileStream.read( data );
			fileStream.close();

		} catch ( FileNotFoundException e ) {
			e.printStackTrace();

		} catch ( IOException e ) {
			e.printStackTrace();
		}

		try {
			javascript = new String( data, "UTF-8" );

		} catch ( UnsupportedEncodingException e ) {
			e.printStackTrace();
		}

		return javascript;
	}


	@Override
	public void onEnable () {

		plugin = this;

		ProxyServer.getPluginManager().registerCommand( this, new reloadCommand() );


		/**
		 * Initialize Javascript Engine.
		 */

		if ( scriptManager == null ) {
			scriptManager = new ScriptEngineManager();
		}

		if ( jsEngine == null ) {
			jsEngine = scriptManager.getEngineByName( "javascript" );
		}

		try {
			jsEngine.put( "PATH", getPluginDir() );
			jsEngine.put( "__plugin", plugin );
			jsEngine.put( "__server", ProxyServer.getInstance() );
			jsEngine.eval( "var global     = {};" );
			jsEngine.eval( "var __instance = {};" );
			jsEngine.eval( "__instance.scope = function ( code ) { return eval( code ); }" );
			jsEngine.eval( "__instance.cleanup = [];" );
			jsEngine.eval( "( function () { \n" + getJSFile() + "\n} ) ();" );

		} catch ( ScriptException e ) {
			e.printStackTrace();
		}
	}


	@Override
	public void onDisable () {

		try {
			jsEngine.eval( ""
				+ "for ( var i in __instance.cleanup ) {" + System.lineSeparator()

				+ "    if ( typeof __instance.cleanup[ i ] === 'function' ) {" + System.lineSeparator()
				+ "        __instance.cleanup[ i ]()" + System.lineSeparator()
				+ "    }" + System.lineSeparator()
				+ "" + System.lineSeparator()
				+ "    __instance.cleanup = [];" + System.lineSeparator()
				+ "}"
			);

		} catch ( ScriptException e ) {
			e.printStackTrace();
		}
	}


	public void reload () {

		onDisable();
		onEnable();
	}
}

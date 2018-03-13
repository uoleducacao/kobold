package io.kobold;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Generate {
	
	interface OnReadLine {
		String onReadLine(String line);
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Iniciado Kobold. Lendo argumentos...");
		final Config config = new Config(args);
		System.out.println("Argumentos lidos");
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
    	System.out.println("Deletando diretórios");
		deleteDirectory(new File(appFolder));
		
		System.out.println("Criando estrutura de pastas do app");
		createAppFoldersStructure(config);
		System.out.println("Estrutura criada");
		System.out.println("Criando resources");
		createFiles(config);
		System.out.println("Resources criados");
		System.out.println("Adicionando modulo em setting.gradle");
		configProject(config);
		System.out.println("Modulo adicionado");
		System.out.println("Kobold finalizado");
	}
	
	private static void configProject(Config config) throws IOException {
		File settingGradle = new File(concatenateFile(config.atenaMdFolder, "settings.gradle"));
		readAndWriteOnFile(config.atenaMdFolder, settingGradle, new OnReadLine() {
			
			@Override
			public String onReadLine(String line) {
				line = line.replace("':" + config.appFolderName + "',", "");
				return line.replace("include", "include " + "':" + config.appFolderName + "',");
			}
		});
	}
	
	private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
            	for (File file : files) {
            		if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
				}
            }
        }
        
        directory.delete();
    }
	
	private static void createAppFoldersStructure(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		createFolder(appFolder);
		createFolder(concatenateFile(appFolder, "src"));
		createFolder(concatenateFile(appFolder, "src/main"));
		createFolder(concatenateFile(appFolder, "src/main/res"));
		createFolder(concatenateFile(appFolder, "src/main/java"));
		createFolder(concatenateFile(appFolder, getPackageFormatted(config)));
	}
	
	private static String getPackageFormatted(Config config) {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		String[] packageStructure = config.packageName.split("\\.");
		String packageFolder = "src/main/java/";
		for (String folder : packageStructure) {
			createFolder(concatenateFile(appFolder, packageFolder + "/" + folder));
			packageFolder += "/" + folder;
		}
		return packageFolder;
	}
	
	private static void createFiles(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		File gitIgnore = new File(concatenateFile(config.configFilesFolder, ".gitignore"));
		Files.copy(gitIgnore.toPath(), new File(concatenateFile(appFolder, ".gitignore")).toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		File glideModule = new File(concatenateFile(config.configFilesFolder, "AppCoreGlideModule.java"));
		readAndWriteOnFile(concatenateFile(appFolder, getPackageFormatted(config)), glideModule, new OnReadLine() {
			
			@Override
			public String onReadLine(String line) {
				return line
						.replace("$package", config.packageName);
			}
		});
		
		File appFile = new File(concatenateFile(config.configFilesFolder, "App.kt"));
		if(!appFile.exists()) {
			appFile = new File(concatenateFile(config.configFilesFolder, "App.java"));
			readAndWriteOnFile(concatenateFile(appFolder, getPackageFormatted(config)), appFile, new OnReadLine() {
				
				@Override
				public String onReadLine(String line) {
					return line
							.replace("$appKeyIdentifier", config.appKeyIdentifier)
							.replace("$package", config.packageName);
				}
			});
		}
		else {
			readAndWriteOnFile(concatenateFile(appFolder, getPackageFormatted(config)), appFile, new OnReadLine() {
				@Override
				public String onReadLine(String line) {
					return line.replace("$package", config.packageName);
				}
			});
		}
		
		createAndroidManifestFile(config);
		createBuildGradle(config);
		createResourcesFolderStructure(config);
	}
	
	private static void createResourcesFolderStructure(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		String resFolder = concatenateFile(appFolder, "src/main/res");
		
		File defaultConfigFilesFolder = new File(concatenateFile(config.configFilesFolder, "defaultConfigFiles"));
		copyAllFiles(defaultConfigFilesFolder, resFolder);
		
		copyImages(config);
		createResourcesFiles(config);
	}
	
	private static void copyImages(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		String resFolder = concatenateFile(appFolder, "src/main/res");
		
		File imageFolder = new File(config.imagesFolder);
		copyAllFiles(imageFolder, resFolder);
	}
	
	private static void copyAllFiles(File sourceFolder, String destinationFolderPath) throws IOException {
		File[] files = sourceFolder.listFiles();
		for (File file : files) {
			if(file.isDirectory()) {
				Files.copy(file.toPath(), new File(concatenateFile(destinationFolderPath, file.getName())).toPath(), StandardCopyOption.REPLACE_EXISTING);
				File[] filesFolder = file.listFiles();
				for (File fileFolder : filesFolder) {
					Files.copy(fileFolder.toPath(), new File(concatenateFile(destinationFolderPath, concatenateFile(fileFolder.getParentFile().getName(), fileFolder.getName()))).toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
			else {
				Files.copy(file.toPath(), new File(concatenateFile(destinationFolderPath, concatenateFile(file.getParentFile().getName(), file.getName()))).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
	
	private static void createResourcesFiles(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		String valuesFolder = concatenateFile(appFolder, "src/main/res/values");
		File stringFile = new File(concatenateFile(config.configFilesFolder, "strings.xml"));
		
		readAndWriteOnFile(valuesFolder, stringFile, new OnReadLine() {
			
			@Override
			public String onReadLine(String line) {
				return line
						.replace("$appName", config.appName)
						.replace("$fcmSenderId", config.fcmSenderId);
			}
		});
	}
	
	private static void createAndroidManifestFile(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		String mainFolder = concatenateFile(appFolder, "src/main");
		File manifestFile = new File(concatenateFile(config.configFilesFolder, "AndroidManifest.xml"));
		
		readAndWriteOnFile(mainFolder, manifestFile, new OnReadLine() {
			
			@Override
			public String onReadLine(String line) {
				return line
						.replace("$package", config.packageName)
						.replace("$pushwoodAppId", config.pushwoodAppId);
			}
		});
	}
	
	private static void createBuildGradle(Config config) throws IOException {
		String appFolder = concatenateFile(config.atenaMdFolder, config.appFolderName);
		File buildGradle = new File(concatenateFile(config.configFilesFolder, "build.gradle"));
		
		readAndWriteOnFile(appFolder, buildGradle, new OnReadLine() {
			
			@Override
			public String onReadLine(String line) {
				if(config.appKey != null && config.keyAlias != null) {
					line = line.replace("$keyAlias", config.keyAlias);
					line = line.replace("$appKey", config.appKey);
				}
				
				return line
						.replace("$package", config.packageName)
						.replace("$clientId", config.clientId)
						.replace("$clientSecret", config.clientSecret)
						.replace("$prodUrl", config.prodUrl)
						.replace("$qaUrl", config.qaUrl);
			}
		});
	}

	private static void readAndWriteOnFile(String folder, File file, OnReadLine onReadLine) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		String fileString = "";
		while ((line = reader.readLine()) != null) {
			fileString += onReadLine.onReadLine(line);
			fileString += "\n";
		}
		reader.close();
		
		File appBuildGradle = new File(concatenateFile(folder, file.getName()));
		appBuildGradle.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(appBuildGradle));
		writer.write(fileString);
		writer.close();
	}
	
	private static void createFolder(String folderName) {
		File file = new File(folderName);
		file.mkdir();
	}
	
	private static String concatenateFile(String path, String conc) {
		return path + "/" + conc;
	}
}

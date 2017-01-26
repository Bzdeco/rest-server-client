package pl.edu.agh.kis.florist.controller;

import pl.edu.agh.kis.florist.dao.FileContentsDAO;
import pl.edu.agh.kis.florist.dao.FileMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FileContents;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import spark.Request;
import spark.Response;

/**
 * Created by bzdeco on 16.01.17.
 */
public class FileMetadataController extends ResourcesController {

    private final FileMetadataDAO fileMetadataDAO = new FileMetadataDAO();
    private final FileContentsDAO fileContentsDAO = new FileContentsDAO();

    @Override
    public Object handleMove(Request request, Response response) {
        // Moved file can be specified by pathLower or pathDisplay
        String oldPath = request.params("path").toLowerCase();
        String newPath = request.queryParams("new_path");
        int ownerID = request.attribute("ownerID");

        // Create file and folder objects from given paths
        File source = (File)File.fromPathLower(oldPath).setOwnerID(ownerID);
        Folder dest = (Folder)Folder.fromPathDisplay(newPath).setOwnerID(ownerID);

        FileMetadata result = fileMetadataDAO.move(source, dest);
        response.status(SUCCESSFUL);
        return result;
    }

    @Override
    public Object handleRename(Request request, Response response) {
        String sourcePathLower = request.params("path").toLowerCase();
        File source = File.fromPathLower(sourcePathLower);

        // FIXME should work
        File fetched = new File(fileMetadataDAO.getMetadata(source));

        String newName = request.queryParams("new_name");
        QueryParameters.validateResourceNameFormat(newName);
        File renamed = File.fromPathDisplay(fetched.getPathDisplayToParent() + newName);

        FileMetadata result = fileMetadataDAO.rename(source, renamed);
        response.status(SUCCESSFUL);
        return result;
    }

    @Override
    public Object handleDelete(Request request, Response response) {
        String deletedFilePath = request.params("path").toLowerCase();
        File deletedFile = File.fromPathLower(deletedFilePath);

        FileMetadata result = fileMetadataDAO.delete(deletedFile);
        response.status(SUCCESSFUL_DELETE);
        return result;
    }

    @Override
    public Object handleGetMetadata(Request request, Response response) {
        String fileLowerPath = request.params("path").toLowerCase();
        File retrieved = File.fromPathLower(fileLowerPath);

        FileMetadata result = fileMetadataDAO.getMetadata(retrieved);
        response.status(SUCCESSFUL);
        return result;
    }

    public Object handleUpload(Request request, Response response) {
        String uploadedFilePathDisplay = request.params("path");
        byte[] uploadedFileContent = request.body().getBytes();

        QueryParameters.validateFilePathFormat(uploadedFilePathDisplay);

        // FileMetadata
        File uploadedFile = File.fromPathDisplay(uploadedFilePathDisplay);
        FileMetadata result = fileMetadataDAO.upload(uploadedFile, uploadedFileContent.length);

        // FileContents
        FileContents fileContents = new FileContents(result.getFileId(), uploadedFileContent);
        fileContentsDAO.upload(fileContents);

        response.status(CREATED);
        return result;
    }

    public Object handleDownload(Request request, Response response) {
        String downloadedFilePathLower = request.params("path").toLowerCase();

        QueryParameters.validateFilePathFormat(downloadedFilePathLower);

        File downloadedFile = File.fromPathLower(downloadedFilePathLower);
        FileMetadata result = fileMetadataDAO.download(downloadedFile);
        String downloadedFileContent = fileContentsDAO.download(downloadedFile);

        response.header("X-File-Metadata", downloadedFileContent);
        response.status(SUCCESSFUL);
        return result;
    }
}

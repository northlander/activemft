package co.nordlander.activemft.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded SFTP server.
 * @author petter
 *
 */
public class EmbeddedSftp {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedSftp.class);
	
    protected SshServer sshd;

    public SshServer getSshServer() {
        return sshd;
    }

    public int getPort() {
        return sshd.getPort();
    }

    /**
     * Create the server.
     * @param port
     * @param username
     * @param password
     * @param fileSystemFolder
     */
    public EmbeddedSftp(int port, String username, String password, File fileSystemFolder) {
        try {
            setupSftp(port, username, password, fileSystemFolder);
        } catch (Exception e) {
            LOGGER.error("Error creating embedded SFTP server, {}", e);
        }
    }

    public void start() throws IOException {
        if (sshd != null) {
            sshd.start();

        }
    }

    public void stop() {
        if (sshd != null) {
            try {
                sshd.stop();
            } catch (InterruptedException ie) {
                LOGGER.warn("Interrupted while stopping SFTP");
            }

        }
    }

    public void close() {
        if (sshd != null) {
            sshd.close(true);
        }
    }

    protected void setupSftp(int port, final String username, final String password, File fileSystemFolder)
        throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        SftpSubsystem.Factory factory = new SftpSubsystem.Factory();

        @SuppressWarnings("unchecked")
        List<NamedFactory<Command>> factoryList = Arrays.<NamedFactory<Command>> asList(new NamedFactory[] {factory});
        this.sshd.setSubsystemFactories(factoryList);

        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String tryUsername, String tryPassword, ServerSession session) {
                return (username.equals(tryUsername)) && (password.equals(tryPassword));
            }

        });
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.start();

        VirtualFileSystemFactory vfSysFactory = new VirtualFileSystemFactory();
        vfSysFactory.setDefaultHomeDir(fileSystemFolder.getCanonicalPath());
        sshd.setFileSystemFactory(vfSysFactory);
        LOGGER.info("Embedded SFTP started on port {}", Integer.valueOf(sshd.getPort()));
    }

}

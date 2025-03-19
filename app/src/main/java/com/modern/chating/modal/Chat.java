package com.modern.chating.modal;

public class Chat {
    public String id;
    public String message;
    public String time;
    public User user;
    public Images images;
    public String audioUrl;
    public File file;
    public Map map;

    public Chat(String id, String message, Map map, Images images, String audioUrl, String time,File file, User user) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.map = map;
        this.user = user;
        this.images = images;
        this.audioUrl = audioUrl;
        this.file = file;

    };

    public File getFile() {
        return file;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public Images getImages() {
        return images;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public User getUser() {
        return user;
    }

    public static class File {
        public String fileUrl, status;
        public String file_type;

        public File(String fileUrl, String file_type, String status){
            this.fileUrl = fileUrl;
            this.file_type = file_type;
            this.status = status;

        }

        public String getFileType() {
            return file_type;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class Map {
        public double latitude ;
        public double longitude;
        public Map(double latitude, double longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public static class Images {
        private String imageUrl;
        private int radius;
        private int blur;
        public Size size;

        private String status = "null";

        public Images(String imageUrl, String status, Size size,int radius, int blur){
            this.imageUrl = imageUrl;
            this.radius = radius;
            this.status = status;
            this.blur = blur;
            this.size = size;
        }

        public Size getSize() {
            return size;
        }

        public String getStatus() {
            return status;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public int getBlur() {
            return blur;
        }

        public int getRadius() {
            return radius;
        }
    }

    public static class User {
        public String id;
        public String name;
        public String avatar;
        public String email;

        public boolean isOnline;

        public User(String id, String name, String email, String avatar, boolean isOnline){
            this.email = email;
            this.id = id;
            this.name = name;
            this.avatar = avatar;
            this.isOnline = isOnline;
        }

        public String getEmail() {
            return email;
        }

        public boolean isOnline() {
            return isOnline;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAvatar() {
            return avatar;
        }
    }

    public static class Size {
        public int width;
        public int height;
        public String fileSize;

        public Size(String fileSize, int width, int height){
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
        }

        public String getFileSize() {
            return fileSize;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}

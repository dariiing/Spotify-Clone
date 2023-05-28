--pentru importat din csv
CREATE OR REPLACE PROCEDURE import_song_data()
AS $$
BEGIN
  CREATE TEMPORARY TABLE temp_song_table (
    id INTEGER,
    song_name VARCHAR(255),
    artist_name VARCHAR(255),
    album_name VARCHAR(255),
    genre VARCHAR(255)
  );

BEGIN
COPY temp_song_table ( id, song_name, artist_name, album_name, genre)
    FROM 'D:\facultate\anul 2\Semestrul 2\programare avansata(java)\Proiect\Spotify-Clone\src\main\resources\songs.csv'
    DELIMITER ',' CSV HEADER;
EXCEPTION
    WHEN OTHERS THEN
      RAISE EXCEPTION 'Error importing data from CSV: %', SQLERRM;
END;

INSERT INTO song_table ( id, song_name, artist_name, album_name, genre)
SELECT  id, song_name, artist_name, album_name, genre
FROM temp_song_table;

DROP TABLE temp_song_table;
END;
$$ LANGUAGE plpgsql;

--pentru importat userii
CREATE OR REPLACE PROCEDURE import_user_data()
AS $$
BEGIN
INSERT INTO application_user ( id, username, name, hashed_password, profile_picture)
VALUES ( '1', 'daria', 'Daria', '$2a$10$xdbKoM48VySZqVSU/cSlVeJn0Z04XCZ7KZBjUBC00eKo5uLswyOpe', NULL);

INSERT INTO application_user ( id, username, name, hashed_password, profile_picture)
VALUES ( '2', 'ana', 'Ana', '$2a$10$xdbKoM48VySZqVSU/cSlVeJn0Z04XCZ7KZBjUBC00eKo5uLswyOpe', NULL);
END;
$$ LANGUAGE plpgsql;

-- verifica genurile
CREATE OR REPLACE FUNCTION validate_genre()
  RETURNS TRIGGER AS $$
BEGIN
  IF NEW.genre NOT IN ('Rock', 'Pop', 'Grunge', 'Funk', 'R&B', 'Latin', 'Pop/Rock', 'Flamenco/Pop', 'Indie Rock', 'A cappella/Pop') THEN
    RAISE EXCEPTION 'Invalid genre: %', NEW.genre;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_genre_trigger
    BEFORE INSERT ON song_table
    FOR EACH ROW
    EXECUTE FUNCTION validate_genre();

-- verifica melodii duplicate din tabelul cu melodii
CREATE OR REPLACE FUNCTION check_duplicate_song()
  RETURNS TRIGGER AS $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM song_table
    WHERE song_name = NEW.song_name
      AND artist_name = NEW.artist_name
      AND album_name = NEW.album_name
  ) THEN
    RAISE EXCEPTION 'Duplicate song entry: % - % - %', NEW.song_name, NEW.artist_name, NEW.album_name;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_duplicate_song_trigger
    BEFORE INSERT ON song_table
    FOR EACH ROW
    EXECUTE FUNCTION check_duplicate_song();


-- recomandari
c


CREATE TRIGGER trigger_delete_liked_song
    AFTER DELETE ON liked_songs
    FOR EACH ROW
    EXECUTE FUNCTION add_recommendations();

CREATE TRIGGER add_recommendations_trigger
    AFTER INSERT ON liked_songs
    FOR EACH ROW
    EXECUTE FUNCTION add_recommendations();

CREATE OR REPLACE FUNCTION delete_duplicate_liked_song()
RETURNS TRIGGER AS
$$
BEGIN
DELETE FROM liked_songs
WHERE song_name = NEW.song_name
  AND artist_name = NEW.artist_name
  AND album_name = NEW.album_name
  AND genre = NEW.genre
  AND user_id = NEW.user_id
  AND id <>(SELECT MAX(id) FROM liked_songs WHERE song_name = NEW.song_name AND artist_name = NEW.artist_name AND album_name = NEW.album_name AND genre = NEW.genre AND user_id = NEW.user_id);

RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER delete_duplicate_liked_song_trigger
    AFTER INSERT ON liked_songs
    FOR EACH ROW
    EXECUTE FUNCTION delete_duplicate_liked_song();


CREATE OR REPLACE FUNCTION check_duplicate_liked_song()
RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM liked_songs
        WHERE song_name = NEW.song_name AND user_id = NEW.user_id
    ) THEN
        RAISE EXCEPTION 'The song is already liked by the user.';
END IF;

RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER prevent_duplicate_liked_song
    BEFORE INSERT ON liked_songs
    FOR EACH ROW
    EXECUTE FUNCTION check_duplicate_liked_song();


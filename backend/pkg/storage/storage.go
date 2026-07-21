package storage

import (
	"fmt"
	"io"
	"os"
	"path/filepath"
)

// Storage defines the interface for file storage operations.
type Storage interface {
	// Save stores a file and returns its path or identifier.
	Save(filename string, reader io.Reader) (string, error)

	// Delete removes a file by its path or identifier.
	Delete(path string) error

	// Open retrieves a file for reading.
	Open(path string) (io.ReadCloser, error)
}

// LocalStorage implements Storage using the local filesystem.
type LocalStorage struct {
	BaseDir string
}

// NewLocalStorage creates a new LocalStorage instance.
func NewLocalStorage(baseDir string) (*LocalStorage, error) {
	absDir, err := filepath.Abs(baseDir)
	if err != nil {
		return nil, fmt.Errorf("resolve base directory: %w", err)
	}

	if err := os.MkdirAll(absDir, 0755); err != nil {
		return nil, fmt.Errorf("create base directory: %w", err)
	}

	return &LocalStorage{BaseDir: absDir}, nil
}

// Save writes a file to the local filesystem.
func (s *LocalStorage) Save(filename string, reader io.Reader) (string, error) {
	fullPath := filepath.Join(s.BaseDir, filename)

	if err := os.MkdirAll(filepath.Dir(fullPath), 0755); err != nil {
		return "", fmt.Errorf("create subdirectory: %w", err)
	}

	file, err := os.Create(fullPath)
	if err != nil {
		return "", fmt.Errorf("create file: %w", err)
	}
	defer file.Close()

	if _, err := io.Copy(file, reader); err != nil {
		return "", fmt.Errorf("write file: %w", err)
	}

	return fullPath, nil
}

// Delete removes a file from the local filesystem.
func (s *LocalStorage) Delete(path string) error {
	return os.Remove(path)
}

// Open retrieves a file for reading.
func (s *LocalStorage) Open(path string) (io.ReadCloser, error) {
	return os.Open(path)
}

package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"runtime"
	"sync"
	"time"

	log "github.com/sirupsen/logrus"
	"github.com/spf13/cobra"
)

type Response struct {
	ID int64 `json:"id"`
}

var (
	FLAG_VERBOSE bool
)
var (
	FLAG_NAME_LOCK        = "lock"
	FLAG_NAME_CONCURRENCY = "concurrency"
	FLAG_NAME_ID          = "id"
)
var (
	DEFAULT_CONCURRENCY       = 10
	DEFAULT_LOCK              = false
	DEFAULT_ID          int64 = -1
)

var client = http.DefaultClient
var wg sync.WaitGroup
var rootCmd = &cobra.Command{
	Use:   "test",
	Short: "Send testing request",
	Run: func(cmd *cobra.Command, args []string) {
		if FLAG_VERBOSE {
			log.SetLevel(log.DebugLevel)
		}

		id, _ := cmd.Flags().GetInt64(FLAG_NAME_ID)
		if id == DEFAULT_ID {
			id = time.Now().Unix()
		}
		lock, _ := cmd.Flags().GetBool(FLAG_NAME_LOCK)
		reqConcurrency, _ := cmd.Flags().GetInt32(FLAG_NAME_CONCURRENCY)

		log.Debugf("id: %d, lock: %t, concurrency: %d", lock, id, reqConcurrency)

		wg.Add(int(reqConcurrency))
		for i := 0; i < int(reqConcurrency); i++ {
			go insert(id, lock)
		}
		wg.Wait()
		getAll()
	},
}

func init() {
	rootCmd.PersistentFlags().Bool(FLAG_NAME_LOCK, DEFAULT_LOCK, "specify request parameter for lock flag")
	rootCmd.PersistentFlags().Int32(FLAG_NAME_CONCURRENCY, int32(DEFAULT_CONCURRENCY), "specify concurrency of requests")
	rootCmd.PersistentFlags().Int64(FLAG_NAME_ID, int64(DEFAULT_ID), "specify request parameter for id")
	rootCmd.PersistentFlags().BoolVarP(&FLAG_VERBOSE, "verbose", "v", false, "print debug logs")
}

func main() {
	log.SetFormatter(&log.TextFormatter{})
	rootCmd.Execute()
}

func insert(id int64, lock bool) {
	req, _ := http.NewRequest("POST", "http://localhost:8080/myentity", nil)
	query := req.URL.Query()
	query.Add("id", fmt.Sprint(id))
	if lock {
		query.Add(FLAG_NAME_LOCK, fmt.Sprint(lock))
	}
	req.URL.RawQuery = query.Encode()
	log.Debugf("POST %s", req.URL.String())
	res, _ := client.Do(req)
	resBytes, _ := ioutil.ReadAll(res.Body)
	runtime.NumGoroutine()
	log.Infof("[%s] %s", res.Status, string(resBytes))

	wg.Done()
}

func getAll() {
	req, _ := http.NewRequest("GET", "http://localhost:8080/myentity", nil)
	log.Debugf("GET %s", req.URL.String())
	res, _ := client.Do(req)
	resBytes, _ := ioutil.ReadAll(res.Body)
	items := make([]Response, 0, 100)
	json.Unmarshal(resBytes, &items)
	log.Infof("%d item(s) found", len(items))
	log.Debugf("Response Body: %d", string(resBytes))
}

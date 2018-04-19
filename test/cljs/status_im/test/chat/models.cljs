(ns status-im.test.chat.models
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.models :as chat]))

(deftest add-chat-test
  (testing "creating a brand new chat"
    (let [chat-id        "some-chat-id"
          contact-name   "contact-name"
          chat-props     {:extra-prop "some"}
          cofx           {:now "now"
                          :db {:deleted-chats     #{"a" chat-id}
                               :contacts/contacts {chat-id
                                                   {:name contact-name}}}}
          response      (chat/add-chat chat-id chat-props cofx)
          deleted-chats (get-in response [:db :deleted-chats])
          actual-chat   (get-in response [:db :chats chat-id])
          store-chat-fx (:data-store/save-chat response)]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it adds the chat id"
        (is (= chat-id (:chat-id actual-chat))))
      (testing "it pulls the name from the contacts"
        (is (= contact-name (:name actual-chat))))
      (testing "it sets the timestamp"
        (is (= "now" (:timestamp actual-chat))))
      (testing "it adds the contact-id to the contact field"
        (is (= chat-id (-> actual-chat :contacts first))))
      (testing "it adds the fx to store a chat"
        (is store-chat-fx))
      (testing "it removes the chat from deleted chats"
        (is (= #{"a"} deleted-chats))))))

(deftest upsert-chat-test
  (testing "upserting an non existing chat"
    (let [chat-id        "some-chat-id"
          contact-name   "contact-name"
          chat-props     {:chat-id chat-id
                          :extra-prop "some"}
          cofx           {:now "now"
                          :db {:contacts/contacts {chat-id
                                                   {:name contact-name}}}}
          response      (chat/upsert-chat chat-props cofx)
          actual-chat   (get-in response [:db :chats chat-id])
          store-chat-fx (:data-store/save-chat response)]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it adds the chat id"
        (is (= chat-id (:chat-id actual-chat))))
      (testing "it pulls the name from the contacts"
        (is (= contact-name (:name actual-chat))))
      (testing "it sets the timestamp"
        (is (= "now" (:timestamp actual-chat))))
      (testing "it adds the contact-id to the contact field"
        (is (= chat-id (-> actual-chat :contacts first))))
      (testing "it adds the fx to store a chat"
        (is store-chat-fx))))
  (testing "upserting an existing chat"
    (let [chat-id        "some-chat-id"
          chat-props     {:chat-id chat-id
                          :name "new-name"
                          :extra-prop "some"}
          cofx           {:db {:chats {chat-id {:name "old-name"}}}}
          response      (chat/upsert-chat chat-props cofx)
          actual-chat   (get-in response [:db :chats chat-id])
          store-chat-fx (:data-store/save-chat response)]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it updates existins props"
        (is (= "new-name" (:name actual-chat))))
      (testing "it adds the fx to store a chat"
        (is store-chat-fx))))
  (testing "upserting a deleted chat"
    (let [chat-id        "some-chat-id"
          contact-name   "contact-name"
          chat-props     {:chat-id chat-id
                          :name "new-name"
                          :extra-prop "some"}
          cofx           {:some-cofx "b"
                          :db {:deleted-chats #{chat-id}
                               :chats {chat-id {:name "old-name"}}}}]
      (testing "it returns the db unchanged"
         (is (= {:db (:db cofx)} (chat/upsert-chat chat-props cofx)))))))

# Highly scalable IRCTC Tatkal Booking System

This project is a prototype for IRCTC Tatkal Booking System.

## Background

[IRCTC](https://en.wikipedia.org/wiki/Indian_Railway_Catering_and_Tourism_Corporation) is a subsidiary of the Indian Railways that handles the catering, tourism and online ticketing operations, with around 5,50,000 to 6,00,000 bookings everyday. IRCTC has more than 20 million user database. Indian railways have Tatkal Ticket booking scheme where certain number of the train seats can only be booked on the day prior to train departure. 'Tatkal' is a hindi word for 'Immediate'. This is handled on online by https://www.irctc.co.in . This Tatkal scheme is used for booking journeys at very short notice.

As of now year 2020 the irctc website allows tatkal booking for sleeper coaches(compartments) from 11:00 A.M IST. As result many people across the country who wants to travel by train on short notice would try booking tickets on 11:00 A.M IST. This leads to tremendous high load on the irctc system which could result in poor response time, high latency for ticket booking orders and in the worst case that the system could crash.

Irctc tatkal booking system is a perfect use case for highly scalable system.
I got fascinated about how one would design and implement such a system that is highly scalable, reliable, maintainable and can process ticket booking requests with low latency.
So, I took this as a challenge to solve. During this challenge I learnt many interesting things and came across interesting ideas on building highly scalable systems.

The goal is to design and build the system that should be highly scalable, reliable and should process many ticket booking orders with low latency.

## Approach

A naive approach is to maintain the data in a RDBMS and have services built on that and these services would process the ticket booking requests using transactional capabilities of RDBMS. But the problem with this approach is that its not scalable. Though you can scale up the services but the RDBMS remains the bottleneck for the entire architecture. This is a traditional architecture.

Another approach is to use horizontally scalable Nosql databases instead of RDBMS.
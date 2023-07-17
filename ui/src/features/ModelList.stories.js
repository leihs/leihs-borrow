import React from 'react'
// import { action } from '@storybook/addon-actions'
import ModelList from './ModelList'

export default {
  title: 'Feature Components/ModelList',
  component: ModelList
}

const exampleProps1 = {
  list: [
    {
      id: '8a57ac64-ebc1-50c2-ba3c-9ddb7df40ac6',
      href: '#borrow/models/8a57ac64-ebc1-50c2-ba3c-9ddb7df40ac6',
      caption: 'Assitasche zu Alexa / Sony',
      subCaption: 'Birkle / Steiner'
    },
    {
      id: '1c18b3d3-88e8-57ac-8c28-24d3f8f77604',
      href: '#borrow/models/1c18b3d3-88e8-57ac-8c28-24d3f8f77604',
      caption: 'AVCHD-Kamera Sony HXR-NX5E',
      subCaption: 'Sony',
      imgSrc: require('../../static/example-images/models/e2d0cfdf-745c-57cb-8c6b-09c14af6bb51.jpg')
    },
    {
      id: 'a004d2f1-81ff-59b6-b3de-668f471fbd07',
      href: '#borrow/models/a004d2f1-81ff-59b6-b3de-668f471fbd07',
      caption: 'AVCHD-Kamera Sony HXR-NX5R',
      isDimmed: true,
      subCaption: 'Sony',
      imgSrc: require('../../static/example-images/models/43cd9671-8ab2-5960-8426-4f9b235b449d.jpg')
    },
    {
      id: '720ecc22-ae14-576b-aa23-c09ee71103ad',
      href: '#borrow/models/720ecc22-ae14-576b-aa23-c09ee71103ad',
      caption: 'BluRay-Player Sony BDP-S490 3D',
      subCaption: 'Sony',
      imgSrc: require('../../static/example-images/models/4d446da0-81ae-5dd4-a2b0-f9adae2de0d4.jpg')
    },
    {
      id: '93afb13c-ef87-528d-a3cd-f53e502c229e',
      href: '#borrow/models/93afb13c-ef87-528d-a3cd-f53e502c229e',
      caption: 'CD-Player Sony CDP-XE370',
      subCaption: 'Sony',
      imgSrc: require('../../static/example-images/models/8e17238d-9ee2-402c-a469-20300d7b08fc.jpg')
    },
    {
      id: '393ddae9-6df5-5f87-ba8c-5a797f3048ad',
      href: '#borrow/models/393ddae9-6df5-5f87-ba8c-5a797f3048ad',
      caption: 'Fernbedienung Kamera Sony RM-1BP',
      isDimmed: true,
      subCaption: 'Sony',
      imgSrc: require('../../static/example-images/models/ad8a36fd-1bec-5be5-a702-326e4e49d756.jpg')
    }
  ]
}

export const modelList = () => <ModelList {...exampleProps1} />

modelList.storyName = 'ModelList'
